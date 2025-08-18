import React from "react";
import Button from "./Button";

export default function Modal({ children, onClose }) {
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">{children}</div>
        {onClose && (
          <div className="modal-actions">
            <Button onClick={onClose}>Schließen</Button>
          </div>
        )}
      </div>
    </div>
  );
}
