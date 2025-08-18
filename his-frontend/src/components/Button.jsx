import React from "react";

export default function Button({ className = "", ...props }) {
  return <button className={`btn ${className}`.trim()} {...props} />;
}
