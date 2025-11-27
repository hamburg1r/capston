import FileItem from "../components/FileItem";
import FileUploadButton from "../components/fileUploadButton";
import { uploadFile } from "../utils/fileApi";


function Dashboard() {
  const [files, setFiles] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchFiles = async () => {
    setLoading(true);
    try {
      const data = await listFiles(token);
      setFiles(data);
    } catch (err) {
      setError(err.message || "Failed to load files");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchFiles(); }, []);

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

      {error && <div className="Error">{error}</div>}

      {loading ? (
        <div className="LoadingFiles">Loading files...</div>
      ) : (
        <div className="FilesList">
          {files.length === 0 && <div>No files yet. Upload one.</div>}
          {files.map((f) => (
            <div key={f.id}>
              <FileItem file={f} />
              {/* optional small preview button to open modal */}
              <button style={{ marginBottom: 8 }}>Open Modal Preview</button>
            </div>
          ))}
        </div>
      )}

    </div>
  )
}

export default Dashboard;
