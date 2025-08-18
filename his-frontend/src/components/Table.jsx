import React from "react";

export default function Table({ className = "", ...props }) {
  return <table className={`table ${className}`.trim()} {...props} />;
}
