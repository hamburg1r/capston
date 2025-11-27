import { createSlice } from "@reduxjs/toolkit";
const initialState = {
  isAuthenticated: false,
  accessToken: null,
  idToken: null,
  profile: null,
};

const authSlice = createSlice({
  name: "auth",
  initialState,
  reducers: {
    setAuth(state, action) {
      const { accessToken, idToken, profile } = action.payload;
      state.isAuthenticated = !!accessToken;
      state.accessToken = accessToken;
      state.idToken = idToken;
      state.profile = profile;
    },
    clearAuth(state) {
      state.isAuthenticated = false;
      state.accessToken = null;
      state.idToken = null;
      state.profile = null;
    },
  },
});

export const { setAuth, clearAuth } = authSlice.actions;
export default authSlice.reducer;
