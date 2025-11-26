import { useRef, useState } from "react";


function FileUploadButton({ onUpload }) {
  const inputRef = useRef();
  const [uploading, setUploading] = useState(false);

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      await onUpload(file);
      // optionally clear input
      inputRef.current.value = "";
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <input
        ref={inputRef}
        type="file"
        // style={{ display: "none" }}
        id="file-upload-input"
        onChange={handleFileChange}
      />
    </div>
  )
}

export default FileUploadButton;
