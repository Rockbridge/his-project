import React from "react";
import { MainLayout } from "../../layout";
import ModulePatientSearch from "./components/ModulePatientSearch";
import PatientDetailSidebar from "./components/PatientDetailSidebar";

export default function PatientSearchPage() {
  const [leftOpen, setLeftOpen] = React.useState(true);
  const [rightOpen, setRightOpen] = React.useState(false);
  const [selectedPatientId, setSelectedPatientId] = React.useState(null);


  React.useEffect(() => {
    setRightOpen(!!selectedPatientId);
  }, [selectedPatientId]);

  const leftContent = (
    <div className="sidebar-section">
      <div className="sidebar-header">
        <strong>Filter</strong>
        <button className="chip" onClick={() => setLeftOpen(false)}>
          ×
        </button>
      </div>
      <div className="sidebar-body">
        {Array.from({ length: 20 }).map((_, i) => (
          <label key={i} className="filter-row">
            <input type="checkbox" /> Option {i + 1}
          </label>
        ))}
      </div>
    </div>
  );

  const rightContent = (
    <div className="sidebar-section">
      <div className="sidebar-header">
        <strong>Patient</strong>
        <button className="chip" onClick={() => setRightOpen(false)}>
          ×
        </button>
      </div>
      <div className="sidebar-body">
        <PatientDetailSidebar patientId={selectedPatientId} />
      </div>
    </div>
  );

  return (
    <MainLayout
      leftOpen={leftOpen}
      rightOpen={rightOpen}
      leftContent={leftContent}
      rightContent={rightContent}
      noCenterScroll={true}
    >
      <ModulePatientSearch
        onSelect={setSelectedPatientId}
        selectedId={selectedPatientId}
      />

    </MainLayout>
  );
}
