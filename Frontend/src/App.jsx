import { useAuth } from "react-oidc-context";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./routes/AppRoutes";
// TODO: Move logic to their dedicated location
function App() {
  const auth = useAuth();

  const signOutRedirect = () => {
    const clientId = "1v2f3obbse4atof6qb0krmrqnu";
    const logoutUri = "http://localhost:5173/";
    const cognitoDomain = "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com";
    window.location.href = `${cognitoDomain}/logout?client_id=${clientId}&logout_uri=${encodeURIComponent(logoutUri)}`;
  };

  if (auth.isLoading) {
    // TODO: add spinner
    return <div>Loading...</div>;
  }

  if (auth.error) {
    return <div>Encountering error... {auth.error.message}</div>;
  }

  if (auth.isAuthenticated) {
    return (
      <div>
        <pre> Hello: {auth.user?.profile.email} </pre>
        <pre> ID Token: {auth.user?.id_token} </pre>
        <pre> Access Token: {auth.user?.access_token} </pre>
        <pre> Refresh Token: {auth.user?.refresh_token} </pre>

        <button onClick={() => auth.removeUser()}>Sign out</button>
      </div>
    );
  }

  return (
    <BrowserRouter>
      <AppRoutes />
    </BrowserRouter>
    // <div>
    //   <button onClick={() => auth.signinRedirect()}>Sign in</button>
    //   <button onClick={() => signOutRedirect()}>Sign out</button>
    // </div>
  );
}

export default App;
