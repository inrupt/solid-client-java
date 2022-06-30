# Inrupt Template TODO: Replace with a relevant README

This template repository provides much of the common structure used by ESS projects.

Once a new project is built from this template a number of changes are required.


1. [GitHub Actions secrets](../../settings/secrets/actions)
   1. Add two sets of secrets:
      - `CLOUDSMITH_TOKEN`
      - `CLOUDSMITH_USERNAME`
2. [GitHub Dependabot secrets](../../settings/secrets/dependabot)
   1. Add two sets of secrets:
      - `CLOUDSMITH_TOKEN`
      - `CLOUDSMITH_USERNAME`
3. [POM](./pom.xml)
   1. Adjust the `<scm>` section (near the bottom).
4. [CI](./.github/workflows/ci-config.yml)
   1. Set the workflow `name` (at the top) to something more informative.
5. [CD](./.github/workflows/cd-config.yml)
   1. Set the workflow `name` (at the top) to something more informative.
   2. Uncomment and adjust the final step of the build job.
   3. Adjust the release tag trigger pattern to match the project name.
6. [Project Site](./src/site/site.xml)
   1. Adjust the PROJECT name with an appropriate value.
7. [README](./README.md) (this file)
   1. Replace with something more relevant to your project.
