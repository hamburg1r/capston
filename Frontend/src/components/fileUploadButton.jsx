import { useRef, useState } from "react";
import "../styles/FileUploadButton.css";

function FileUploadButton({ onUpload }) {
  const inputRef = useRef();
  const [uploading, setUploading] = useState(false);

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    setUploading(true);

    try {
      await onUpload(file);
      inputRef.current.value = "";
    } finally {
      setUploading(false);
    }
  };

  const openFilePicker = () => {
    if (!uploading) inputRef.current.click();
  };

  return (
    <div className="upload-btn-wrapper">
      <input
        ref={inputRef}
        type="file"
        id="file-upload-input"
        className="hidden-file-input"
        onChange={handleFileChange}
      />

      <button
        className={`browse-btn ${uploading ? "disabled" : ""}`}
        onClick={openFilePicker}
        disabled={uploading}
      >
        {uploading ? "Uploading..." : "Choose File"}
      </button>
    </div>
  );
}

export default FileUploadButton;
