export default {
  Auth: {
    Cognito: {
      userPoolId: "us-east-1_eOYo85Ex9",              
      userPoolClientId: "1v2f3obbse4atof6qb0krmrqnu", 
      identityPoolId: undefined,
      loginWith: {
        oauth: {
          domain: "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com",
          scopes: ["openid", "email", "profile"],
          redirectSignIn: ["http://localhost:5173/"],
          redirectSignOut: ["http://localhost:5173/"],
          responseType: "code"
        }
      },
      region: "us-east-1"
    }
  }
};
