#!/bin/bash
# setup-elk.sh - ELK Stack Konfiguration erstellen

set -e

echo "🔍 Setting up ELK Stack Configuration..."

# Farben für Output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

# 1. Verzeichnisse erstellen
print_status "Creating ELK configuration directories..."
mkdir -p elk-config/logstash/config
mkdir -p elk-config/logstash/pipeline
mkdir -p elk-config/kibana
mkdir -p elk-config/filebeat
mkdir -p logs/elk

print_success "Directories created"

# 2. Logstash Konfiguration
print_status "Creating Logstash configuration..."
cat > elk-config/logstash/config/logstash.yml << 'EOF'
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.hosts: [ "http://elasticsearch:9200" ]
path.config: /usr/share/logstash/pipeline
EOF

# 3. Logstash Pipeline für HIS Services
print_status "Creating Logstash pipeline..."
cat > elk-config/logstash/pipeline/logstash.conf << 'EOF'
input {
  # Filebeat input
  beats {
    port => 5044
  }
  
  # TCP input für direkte Service-Logs
  tcp {
    port => 5001
    codec => json_lines
  }
  
  # UDP input (optional)
  udp {
    port => 5001
    codec => json_lines
  }
}

filter {
  # Service-spezifische Filter
  if [fields][service] {
    mutate {
      add_field => { "service_name" => "%{[fields][service]}" }
    }
  }
  
  # Docker Labels verarbeiten
  if [container][labels][service] {
    mutate {
      add_field => { "service_name" => "%{[container][labels][service]}" }
    }
  }
  
  # Log Level extrahieren
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:log_message}" }
    tag_on_failure => ["_grokparsefailure"]
  }
  
  # Timestamp parsen
  date {
    match => [ "timestamp", "yyyy-MM-dd HH:mm:ss.SSS" ]
  }
  
  # Service-spezifische Enrichments
  if [service_name] == "api-gateway" {
    mutate {
      add_field => { "component" => "gateway" }
      add_field => { "system" => "his" }
    }
  }
  
  if [service_name] == "patient-service" {
    mutate {
      add_field => { "component" => "patient" }
      add_field => { "system" => "his" }
    }
  }
  
  if [service_name] == "encounter-service" {
    mutate {
      add_field => { "component" => "encounter" }
      add_field => { "system" => "his" }
    }
  }
}

output {
  # Elasticsearch output
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "his-logs-%{+YYYY.MM.dd}"
    template_name => "his-logs"
    template_pattern => "his-logs-*"
    template => {
      "index_patterns" => ["his-logs-*"]
      "settings" => {
        "number_of_shards" => 1
        "number_of_replicas" => 0
      }
      "mappings" => {
        "properties" => {
          "@timestamp" => { "type" => "date" }
          "service_name" => { "type" => "keyword" }
          "component" => { "type" => "keyword" }
          "level" => { "type" => "keyword" }
          "logger" => { "type" => "keyword" }
          "thread" => { "type" => "keyword" }
          "message" => { "type" => "text" }
          "log_message" => { "type" => "text" }
        }
      }
    }
  }
  
  # Debug output (kann später entfernt werden)
  stdout { 
    codec => rubydebug 
  }
}
EOF

# 4. Kibana Konfiguration
print_status "Creating Kibana configuration..."
cat > elk-config/kibana/kibana.yml << 'EOF'
server.name: kibana
server.host: 0.0.0.0
server.port: 5601
elasticsearch.hosts: [ "http://elasticsearch:9200" ]
monitoring.ui.container.elasticsearch.enabled: true

# Dashboard und Index Pattern Konfiguration
kibana.index: ".elvs-kibana"
logging.appenders:
  console:
    type: console
    layout:
      type: json
EOF

# 5. Filebeat Konfiguration
print_status "Creating Filebeat configuration..."
cat > elk-config/filebeat/filebeat.yml << 'EOF'
filebeat.inputs:
# HIS Application Logs
- type: log
  enabled: true
  paths:
    - /usr/share/filebeat/logs/gateway/*.log
    - /usr/share/filebeat/logs/patient/*.log
    - /usr/share/filebeat/logs/encounter/*.log
  fields:
    logtype: application
    system: his
  fields_under_root: true
  multiline.pattern: '^\d{4}-\d{2}-\d{2}'
  multiline.negate: true
  multiline.match: after

# Docker Container Logs
- type: container
  enabled: true
  paths:
    - '/var/lib/docker/containers/*/*.log'
  processors:
    - add_docker_metadata:
        host: "unix:///var/run/docker.sock"

# Output zu Logstash
output.logstash:
  hosts: ["logstash:5044"]

# Processors
processors:
  - add_host_metadata:
      when.not.contains.tags: forwarded
  - add_docker_metadata: ~
  - add_kubernetes_metadata: ~

# Logging Level
logging.level: info
logging.to_files: true
logging.files:
  path: /var/log/filebeat
  name: filebeat
  keepfiles: 7
  permissions: 0644
EOF

# 6. Elasticsearch Index Template erstellen
print_status "Creating Elasticsearch index template..."
cat > elk-config/elasticsearch-template.json << 'EOF'
{
  "index_patterns": ["his-logs-*"],
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0,
    "index.refresh_interval": "5s"
  },
  "mappings": {
    "properties": {
      "@timestamp": {
        "type": "date"
      },
      "service_name": {
        "type": "keyword"
      },
      "component": {
        "type": "keyword"
      },
      "level": {
        "type": "keyword"
      },
      "logger": {
        "type": "keyword"
      },
      "thread": {
        "type": "keyword"
      },
      "message": {
        "type": "text",
        "analyzer": "standard"
      },
      "log_message": {
        "type": "text",
        "analyzer": "standard"
      },
      "container": {
        "properties": {
          "name": {"type": "keyword"},
          "id": {"type": "keyword"}
        }
      }
    }
  }
}
EOF

# 7. Kibana Dashboard-Konfiguration erstellen
print_status "Creating Kibana dashboard configuration..."
mkdir -p elk-config/kibana/dashboards

cat > elk-config/kibana/dashboards/his-dashboard.json << 'EOF'
{
  "dashboard": {
    "id": "his-overview",
    "title": "HIS System Overview",
    "description": "Hospital Information System - Service Overview",
    "version": 1,
    "timeRestore": true,
    "timeTo": "now",
    "timeFrom": "now-1h"
  }
}
EOF

print_success "ELK configuration files created successfully!"

echo ""
echo "📋 Created Configuration Files:"
echo "  • elk-config/logstash/config/logstash.yml"
echo "  • elk-config/logstash/pipeline/logstash.conf"
echo "  • elk-config/kibana/kibana.yml"
echo "  • elk-config/filebeat/filebeat.yml"
echo "  • elk-config/elasticsearch-template.json"
echo ""
echo "🚀 Next Steps:"
echo "  1. Update docker-compose to use original ELK configuration"
echo "  2. Start ELK services: docker-compose -f docker-compose.dev.yml up elk"
echo "  3. Access Kibana: http://localhost:5601"
echo ""