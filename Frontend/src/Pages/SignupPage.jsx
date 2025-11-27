import React from "react";
import Navbar from "../components/Navbar";

export default function SignupPage() {
  const clientId = "1v2f3obbse4atof6qb0krmrqnu";
  const redirect = "http://localhost:5173/";
  const domain = "https://us-east-1eoyo85ex9.auth.us-east-1.amazoncognito.com";

  const signupUrl = `${domain}/signup?client_id=${clientId}&response_type=code&redirect_uri=${"http://localhost:5173/login"}`;

  return (
    <div>
      <Navbar />
      <div className="max-w-md mx-auto mt-20 p-6 bg-white rounded shadow">
        <h2 className="text-2xl mb-4">Sign Up</h2>
        <a href={signupUrl} className="inline-block w-full">
          <button className="w-full py-2 bg-green-600 text-white rounded">Open Hosted UI - Sign up</button>
        </a>
        <p className="mt-4 text-sm text-gray-500">If a user was not found they'll be guided here.</p>
      </div>
    </div>
  );
}
