import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axiosClient from "../../api/axiosClient";

export const fetchFiles = createAsyncThunk(
  "files/fetchFiles",
  async ({ token }, { rejectWithValue }) => {
    try {
      const res = await axiosClient.get("/api/documents", {
        headers: { Authorization: `Bearer ${token}` },
      });
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

// request presigned url from backend
export const requestPresigned = createAsyncThunk(
  "files/requestPresigned",
  async ({ token, fileName, fileType }, { rejectWithValue }) => {
    try {
      const res = await axiosClient.post(
        "/api/documents/presigned-url",
        { fileName, fileType },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      return res.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

const filesSlice = createSlice({
  name: "files",
  initialState: {
    items: [],
    loading: false,
    error: null,
    presign: null,
  },
  reducers: {
    addFileOptimistic(state, action) {
      state.items.unshift(action.payload);
    },
    updateFileStatus(state, action) {
      const { documentId, status } = action.payload;
      state.items = state.items.map((f) =>
        f.documentId === documentId ? { ...f, status } : f
      );
    },

    deleteByDocumentId(state, action){
      const id = action.payload;
      return {...state, items: state.items.filter(item => item.documentId !== id)};
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchFiles.pending, (s) => {
        s.loading = true;
        s.error = null;
      })
      .addCase(fetchFiles.fulfilled, (s, a) => {
        s.loading = false;
        s.items = a.payload;
      })
      .addCase(fetchFiles.rejected, (s, a) => {
        s.loading = false;
        s.error = a.payload;
      })
      .addCase(requestPresigned.pending, (s) => {
        s.presign = { loading: true };
      })
      .addCase(requestPresigned.fulfilled, (s, a) => {
        s.presign = { loading: false, data: a.payload };
      })
      .addCase(requestPresigned.rejected, (s, a) => {
        s.presign = { loading: false, error: a.payload };
      });
  },
});

export const { addFileOptimistic, updateFileStatus , deleteByDocumentId} = filesSlice.actions;
export default filesSlice.reducer;
