#!/bin/bash

# =============================================================================
# Fix: Leere/Beschädigte Liquibase Changelog-Dateien reparieren
# =============================================================================

set -euo pipefail

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log() { echo -e "${GREEN}[FIX]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

cd his-patient-service

# =============================================================================
# 1. Prüfe aktuelle Changelog-Dateien
# =============================================================================
log "Checking current changelog files..."

echo "=== File sizes ==="
ls -la src/main/resources/db/changelog/

echo "=== Content check ==="
for file in src/main/resources/db/changelog/*.xml; do
    echo "File: $file"
    echo "Size: $(wc -c < "$file") bytes"
    echo "First line: $(head -1 "$file" 2>/dev/null || echo 'EMPTY')"
    echo "---"
done

# =============================================================================
# 2. Erstelle korrekte Master Changelog
# =============================================================================
log "Creating correct db.changelog-master.xml..."

cat > src/main/resources/db/changelog/db.changelog-master.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <!-- Master Changelog für Patient Service -->
    <!-- Ausführungsreihenfolge ist kritisch! -->
    
    <include file="db/changelog/001-create-persons-table.xml"/>
    <include file="db/changelog/002-create-patients-table.xml"/>
    <include file="db/changelog/003-create-addresses-table.xml"/>
    <include file="db/changelog/004-create-indexes.xml"/>
    
</databaseChangeLog>
EOF

# =============================================================================
# 3. Erstelle 001-create-persons-table.xml
# =============================================================================
log "Creating 001-create-persons-table.xml..."

cat > src/main/resources/db/changelog/001-create-persons-table.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <!-- Schema: his_patient -->
    <changeSet id="001-create-persons-table" author="his-team">
        <comment>Create persons table - Base entity for all person types</comment>
        
        <!-- UUID Extension aktivieren -->
        <sql>CREATE EXTENSION IF NOT EXISTS "uuid-ossp";</sql>
        
        <!-- Persons Table -->
        <createTable tableName="persons" schemaName="his_patient">
            <!-- Primary Key -->
            <column name="id" type="UUID" defaultValueComputed="uuid_generate_v4()">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            
            <!-- Basic Person Information -->
            <column name="first_name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="last_name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
            <column name="title" type="VARCHAR(50)">
                <constraints nullable="true"/>
            </column>
            
            <!-- Demographics -->
            <column name="birth_date" type="DATE">
                <constraints nullable="true"/>
            </column>
            <column name="gender" type="VARCHAR(20)">
                <constraints nullable="true"/>
            </column>
            
            <!-- Contact Information -->
            <column name="phone" type="VARCHAR(50)">
                <constraints nullable="true"/>
            </column>
            <column name="email" type="VARCHAR(100)">
                <constraints nullable="true"/>
            </column>
            
            <!-- Audit Fields -->
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="version" type="BIGINT" defaultValue="0">
                <constraints nullable="false"/>
            </column>
            
            <!-- Soft Delete -->
            <column name="deleted_at" type="TIMESTAMP">
                <constraints nullable="true"/>
            </column>
        </createTable>
        
        <!-- Gender Check Constraint -->
        <sql>
            ALTER TABLE his_patient.persons 
            ADD CONSTRAINT chk_persons_gender 
            CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'UNKNOWN'));
        </sql>
        
        <!-- Trigger für automatisches updated_at -->
        <sql>
            CREATE OR REPLACE FUNCTION his_patient.update_updated_at_column()
            RETURNS TRIGGER AS $$
            BEGIN
                NEW.updated_at = CURRENT_TIMESTAMP;
                RETURN NEW;
            END;
            $$ LANGUAGE 'plpgsql';
            
            CREATE TRIGGER tr_persons_updated_at
                BEFORE UPDATE ON his_patient.persons
                FOR EACH ROW
                EXECUTE FUNCTION his_patient.update_updated_at_column();
        </sql>
        
        <rollback>
            DROP TRIGGER IF EXISTS tr_persons_updated_at ON his_patient.persons;
            DROP FUNCTION IF EXISTS his_patient.update_updated_at_column();
            DROP TABLE his_patient.persons;
        </rollback>
    </changeSet>
    
</databaseChangeLog>
EOF

# =============================================================================
# 4. Erstelle 002-create-patients-table.xml
# =============================================================================
log "Creating 002-create-patients-table.xml..."

cat > src/main/resources/db/changelog/002-create-patients-table.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="002-create-patients-table" author="his-team">
        <comment>Create patients table - Patient-specific data extending persons</comment>
        
        <!-- Patients Table (Extends Persons via JPA @Inheritance JOINED) -->
        <createTable tableName="patients" schemaName="his_patient">
            <!-- Primary Key - References persons.id -->
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            
            <!-- German Healthcare Identifiers -->
            <column name="kvnr" type="VARCHAR(50)">
                <constraints nullable="false" unique="true"/>
            </column>
            
            <!-- Insurance Information -->
            <column name="insurance_number" type="VARCHAR(100)">
                <constraints nullable="true"/>
            </column>
            <column name="insurance_status" type="VARCHAR(50)">
                <constraints nullable="true"/>
            </column>
            <column name="insurance_type" type="VARCHAR(50)">
                <constraints nullable="true"/>
            </column>
            <column name="insurance_company_id" type="VARCHAR(100)">
                <constraints nullable="true"/>
            </column>
            <column name="insurance_company_name" type="VARCHAR(200)">
                <constraints nullable="true"/>
            </column>
            
            <!-- DSGVO Consent Management -->
            <column name="consent_communication" type="BOOLEAN" defaultValue="false">
                <constraints nullable="false"/>
            </column>
            <column name="consent_data_processing" type="BOOLEAN" defaultValue="false">
                <constraints nullable="false"/>
            </column>
        </createTable>
        
        <!-- Foreign Key zu persons -->
        <addForeignKeyConstraint 
            baseTableSchemaName="his_patient" baseTableName="patients" baseColumnNames="id"
            referencedTableSchemaName="his_patient" referencedTableName="persons" referencedColumnNames="id"
            constraintName="fk_patients_persons"
            onDelete="CASCADE"/>
        
        <rollback>
            DROP TABLE his_patient.patients;
        </rollback>
    </changeSet>
    
</databaseChangeLog>
EOF

# =============================================================================
# 5. Erstelle 003-create-addresses-table.xml
# =============================================================================
log "Creating 003-create-addresses-table.xml..."

cat > src/main/resources/db/changelog/003-create-addresses-table.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="003-create-addresses-table" author="his-team">
        <comment>Create addresses table</comment>
        
        <createTable tableName="addresses" schemaName="his_patient">
            <column name="id" type="UUID" defaultValueComputed="uuid_generate_v4()">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="person_id" type="UUID">
                <constraints nullable="false"/>
            </column>
            <column name="address_type" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="street" type="VARCHAR(200)">
                <constraints nullable="true"/>
            </column>
            <column name="house_number" type="VARCHAR(10)">
                <constraints nullable="true"/>
            </column>
            <column name="postal_code" type="VARCHAR(10)">
                <constraints nullable="true"/>
            </column>
            <column name="city" type="VARCHAR(100)">
                <constraints nullable="true"/>
            </column>
            <column name="state" type="VARCHAR(100)">
                <constraints nullable="true"/>
            </column>
            <column name="country" type="VARCHAR(50)" defaultValue="DE">
                <constraints nullable="false"/>
            </column>
            <column name="created_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="updated_at" type="TIMESTAMP" defaultValueComputed="CURRENT_TIMESTAMP">
                <constraints nullable="false"/>
            </column>
            <column name="version" type="BIGINT" defaultValue="0">
                <constraints nullable="false"/>
            </column>
            <column name="deleted_at" type="TIMESTAMP">
                <constraints nullable="true"/>
            </column>
        </createTable>
        
        <addForeignKeyConstraint 
            baseTableSchemaName="his_patient" baseTableName="addresses" baseColumnNames="person_id"
            referencedTableSchemaName="his_patient" referencedTableName="persons" referencedColumnNames="id"
            constraintName="fk_addresses_persons"
            onDelete="CASCADE"/>
        
        <rollback>
            DROP TABLE his_patient.addresses;
        </rollback>
    </changeSet>
    
</databaseChangeLog>
EOF

# =============================================================================
# 6. Erstelle 004-create-indexes.xml
# =============================================================================
log "Creating 004-create-indexes.xml..."

cat > src/main/resources/db/changelog/004-create-indexes.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="004-create-indexes" author="his-team">
        <comment>Create performance indexes</comment>
        
        <!-- Name Search Index -->
        <createIndex tableName="persons" schemaName="his_patient" indexName="idx_persons_name_search">
            <column name="last_name"/>
            <column name="first_name"/>
        </createIndex>
        
        <!-- KVNR Index -->
        <createIndex tableName="patients" schemaName="his_patient" indexName="idx_patients_kvnr">
            <column name="kvnr"/>
        </createIndex>
        
        <!-- Address Person Index -->
        <createIndex tableName="addresses" schemaName="his_patient" indexName="idx_addresses_person_id">
            <column name="person_id"/>
        </createIndex>
        
        <rollback>
            DROP INDEX IF EXISTS his_patient.idx_addresses_person_id;
            DROP INDEX IF EXISTS his_patient.idx_patients_kvnr;
            DROP INDEX IF EXISTS his_patient.idx_persons_name_search;
        </rollback>
    </changeSet>
    
</databaseChangeLog>
EOF

# =============================================================================
# 7. Validiere XML-Dateien
# =============================================================================
log "Validating XML files..."

for file in src/main/resources/db/changelog/*.xml; do
    echo "Validating: $file"
    if command -v xmllint >/dev/null 2>&1; then
        xmllint --noout "$file" && echo "✅ Valid" || echo "❌ Invalid"
    else
        echo "⚠️ xmllint not available, skipping validation"
    fi
done

# =============================================================================
# 8. Teste Liquibase
# =============================================================================
log "Testing Liquibase with fixed files..."

mvn liquibase:status \
  -Dliquibase.url=jdbc:postgresql://localhost:5432/his_db \
  -Dliquibase.username=his_user \
  -Dliquibase.password=dev_password \
  -Dliquibase.driver=org.postgresql.Driver \
  -Dliquibase.changeLogFile=src/main/resources/db/changelog/db.changelog-master.xml \
  -Dliquibase.defaultSchemaName=his_patient

log "✅ All changelog files created and validated!"

echo
log "Next step: Run 'mvn liquibase:update' to apply migrations"

cd ..