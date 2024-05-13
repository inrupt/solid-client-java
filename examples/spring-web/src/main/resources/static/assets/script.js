(function() {
  // Clear any state in the URL
  history.pushState({}, "", "/");

  // General API request handler
  const get = (uri) => {
    return fetch(uri).then(res => {
      if (res.ok) {
        return res.headers.get("Content-Type") === "application/json" ? res.json() : res.text();
      }
      throw new Error("Error fetching data");
    });
  };

  // Specific API methods
  const getUser = () => get("/api/webid");
  const getProfile = () => get("/api/profile");
  const getResource = (uri) => get(`/api/resource?uri=${uri}`);

  // Load the application data
  getUser()
    .then(user => {
        document.getElementById("user").innerHTML = `<a href="/logout">logout</a>`;
        document.getElementById("webid").value = user.id;
      })
    .then(x => getProfile())
    .then(profile => {
        document.getElementById("storage").value = profile.storages[0];
        document.getElementById("load").disabled = false;
        document.getElementById("load").addEventListener('click', evt => {
          document.getElementById("fetch-output").value = "Loading...";
          getResource(profile.storages[0])
            .then(resource => document.getElementById("fetch-output").value = resource)
            .catch(err => window.location.href = "/");
        });
      })
    .catch(err => document.getElementById("webid").innerHTML = `<a href="/oauth2/authorization/myApp">login</a>`);
})();
