import React from "react";
import { useSelection } from "../../state/selection";
import { MainLayout } from "../../layout";
import ModulePatientSearch from "./components/ModulePatientSearch";
import PatientDetailSidebar from "./components/PatientDetailSidebar";

export default function PatientSearchPage() {
  const { selectedPatientId } = useSelection();
  const [leftOpen, setLeftOpen] = React.useState(true);
  const [rightOpen, setRightOpen] = React.useState(false);

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
        <PatientDetailSidebar />
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
      <ModulePatientSearch />
    </MainLayout>
  );
}
