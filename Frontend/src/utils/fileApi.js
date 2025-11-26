export async function listFiles(token) {
  if (!token) throw new Error("No token provided")

  const res = await fetch("", {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });
  if (!res.ok) throw new Error("Failed to list files");
  return res.json();
}

export async function upload(token, file) {
  if (!token) throw new Error("No token provided")

  const form = new FormData();
  form.append("file", file);

  const res = await fetch("", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
    },
    body: form,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error("Upload failed: " + text);
  }
  return res.json();
}
