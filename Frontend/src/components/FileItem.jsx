import React from "react";
import { useNavigate } from "react-router-dom";

export default function FileItem({ file }) {
  const nav = useNavigate();

  const openPreview = () => {
    // navigate to preview page
    nav(`/files/${file.id}`);
  };

  return (
    <div>
      <div>
        <div>{file.name}</div>
        <div>{file.mimeType}</div>
        <div>{file.size}</div>
      </div>

      <div>
        <button onClick={openPreview}>Preview</button>
        <a href={file.url} download={file.name}>
          <button>Download</button>
        </a>
      </div>
    </div>
  );
}
