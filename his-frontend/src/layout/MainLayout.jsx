import React from "react";

/**
 * MainLayout: generischer Hauptbereich mit optionalen Sidebars (links/rechts)
 */
export default function MainLayout({
  leftOpen,
  rightOpen,
  leftContent,
  rightContent,
  children,
  noCenterScroll = false,
}) {
  const cls = [
    "app-main",
    leftOpen ? "with-left" : "no-left",
    rightOpen ? "with-right" : "no-right",
  ].join(" ");

  return (
    <main className={cls} role="main" aria-label="Hauptbereich">
      {leftOpen && (
        <aside
          className="app-sidebar app-sidebar-left"
          aria-label="Linke Seitenleiste"
        >
          <div className="sidebar-scroll">{leftContent}</div>
        </aside>
      )}

      {/* Center */}
      <section className="app-center" aria-label="Zentralbereich">
        <div
          className={`center-scroll ${
            noCenterScroll ? "no-center-scroll" : ""
          }`}
        >
          {children}
        </div>
      </section>

      {rightOpen && (
        <aside
          className="app-sidebar app-sidebar-right"
          aria-label="Rechte Seitenleiste"
        >
          <div className="sidebar-scroll">{rightContent}</div>
        </aside>
      )}
    </main>
  );
}
