import React from "react";

export default function Header() {
  return (
    <header className="app-header" role="banner">
      <div className="app-logo">Hospital Information System</div>
      <div className="app-header-meta">
        <span className="app-user">Dr. Schmidt</span>
        <span className="app-role">Physician</span>
      </div>
    </header>
  );
}
