(async function() {
  // Clear any state in the URL
  history.pushState({}, "", "/");

  // General API request handler
  async function get(uri) {
    const res = await fetch(uri);
    if (res.ok) {
      return res.headers.get("Content-Type") === "application/json" ? res.json() : res.text();
    }
    throw new Error("Error fetching data");
  }

  // Specific API methods
  function getUser() {
    return get("/api/webid");
  }
  function getProfile() {
    return get("/api/profile");
  }
  function getResource(uri) {
    return get(`/api/resource?uri=${uri}`);
  }

  try {
    // Load the application data
    const user = await getUser();
    document.getElementById("user").innerHTML = `<a href="/logout">logout</a>`;
    document.getElementById("webid").value = user.id;

    const profile = await getProfile();
    document.getElementById("storage").value = profile.storages[0];
    document.getElementById("load").disabled = false;
    document.getElementById("load").addEventListener('click', async evt => {
      document.getElementById("fetch-output").value = "Loading...";
      try {
        const resource = await getResource(new URL(profile.storages[0]));
        document.getElementById("fetch-output").value = resource;
      } catch (error) {
          // An error here means that the access token has timed out
          window.location.href = "/";
      }
    });
  } catch (error) {
    // An error here means that the user is not logged in. In that case, display a login link.
    document.getElementById("webid").innerHTML = `<a href="/oauth2/authorization/myApp">login</a>`;
  }
})();
