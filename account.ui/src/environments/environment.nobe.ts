// use 'ng serve --configuration nobe' to serve static json file from assets (GET requests only)
export const environment = {
  production: false,

  accountapi_host: "http://localhost:4200",
  accountapi_app: "/accountapi/",
  envName: ' (NODB)',
  uiversion: ' dbal',
  folder: '/assets',
  accountapi_ext: ".json"
};
