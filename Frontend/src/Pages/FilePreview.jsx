import axios from "axios";
import React, { useEffect, useState } from "react";
import { useAuth } from "react-oidc-context";
import { useParams } from "react-router-dom";
import FileItem from "../components/FileItem";
import Navbar from "../components/Navbar";

function FilePreview() {
  const { id } = useParams();
  const auth = useAuth();

  const [file, setFile] = useState(null);

  useEffect(() => {
    const fetchFile = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8081/api/documents/${id}`,
          {
            headers: {
              Authorization: `Bearer ${auth.user?.id_token}`,
            },
          }
        );

        console.log("resData:", res.data);
        setFile(res.data);
      } catch (error) {
        console.log("error:", error);
      }
    };

    if (auth.user) fetchFile();
  }, [id, auth.user]);

  if (!file) return <div>Loading file...</div>;

  return (
    <div>
       <Navbar />
      <FileItem file={file} />
    </div>
  );
}

export default FilePreview;
