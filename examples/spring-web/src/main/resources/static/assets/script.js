(async function() {
  // Clear any state in the URL
  history.pushState({}, "", "/");

  // General API request handler
  async function get(uri) {
    return fetch(uri).then(res => {
      if (res.ok) {
        return res.headers.get("Content-Type") === "application/json" ? res.json() : res.text();
      }
      throw new Error("Error fetching data");
    });
  }

  // Specific API methods
  async function getUser() {
    return get("/api/webid");
  }
  async function getProfile() {
    return get("/api/profile");
  }
  async function getResource(uri) {
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
    document.getElementById("load").addEventListener('click', evt => {
      document.getElementById("fetch-output").value = "Loading...";
      getResource(new URL(profile.storages[0]))
          .then(resource => document.getElementById("fetch-output").value = resource)
          .catch(err => window.location.href = "/");
    });
  } catch (error) {
    // An error means that the user is not logged in. In that case, display a login link.
    document.getElementById("webid").innerHTML = `<a href="/oauth2/authorization/myApp">login</a>`;
  }
})();
