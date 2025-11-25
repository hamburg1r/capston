import FileUploadButton from "../components/fileUploadButton";


function Dashboard() {
  return (
    <div className="Dashboard">
      {/* <button onClick={}> upload </button> */}
      <FileUploadButton onUpload={(file) => console.log(file)} />
    </div>
  )
}

export default Dashboard;
