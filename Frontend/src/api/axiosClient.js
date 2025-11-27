// axios client that attaches token from store or passed getter
import axios from "axios";

const API_BASE =  import.meta.env.REACT_APP_API_BASE || "http://localhost:8081";

const axiosClient = axios.create({
  baseURL: API_BASE,
  timeout: 30000,
});

export default axiosClient;
