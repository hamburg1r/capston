import FileUploadButton from "../components/fileUploadButton";
import { uploadFile } from "../utils/fileApi";


function Dashboard() {
  const [files, setFiles] = useState([]);

  const handleUpload = async (file) => {
    try {
      const created = await uploadFile(token, file);
      // TODO: if backend returns url, push to list
      setFiles((s) => [created, ...s]);
    } catch (err) {
      alert("Upload failed: " + (err.message || err));
    }
  };

  return (
    <div className="Dashboard">
      {/* <button onClick={}> upload </button> */}
      <FileUploadButton onUpload={handleUpload} />
    </div>
  )
}

export default Dashboard;
