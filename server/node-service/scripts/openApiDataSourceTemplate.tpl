import { readYaml } from "../../common/util";
import _ from "lodash";
import path from "path";
import { OpenAPIV3, OpenAPI } from "openapi-types";
import { ConfigToType, DataSourcePlugin } from "openblocks-sdk/dataSource";
import { runOpenApi } from "../openApi";
import { parseOpenApi, ParseOpenApiOptions } from "../openApi/parse";
<% if (isJsonSpec) {%>
import spec from './<%=id %>.spec.json';
<% } %>

<% if (isYamlSpec) {%>
const spec = readYaml(path.join(__dirname, "./<%=id %>.spec.yaml"));
<% } %>

const dataSourceConfig = {
  type: "dataSource",
  params: <%=dataSourceParams %>
} as const;

const parseOptions: ParseOpenApiOptions = {
  actionLabel: (method: string, path: string, operation: OpenAPI.Operation) => {
    return _.upperFirst(operation.operationId || "");
  },
};

type DataSourceConfigType = ConfigToType<typeof dataSourceConfig>;

const <%=id %>Plugin: DataSourcePlugin<any, DataSourceConfigType> = {
  id: "<%=id %>",
  name: "<%=name %>",
  icon: "<%=id %>.svg",
  category: "api",
  dataSourceConfig,
  queryConfig: async () => {
    const { actions, categories } = await parseOpenApi(spec<% if (isJsonSpec) { %> as unknown<% } %> as OpenAPI.Document, parseOptions);
    return {
      type: "query",
      label: "Action",
      categories: {
        label: "Resources",
        items: categories,
      },
      actions,
    };
  },
  run: function (actionData, dataSourceConfig): Promise<any> {
    const runApiDsConfig = {
      url: "",
      serverURL: "",
      dynamicParamsConfig: dataSourceConfig,
    };
    return runOpenApi(actionData, runApiDsConfig, spec as OpenAPIV3.Document);
  },
};

export default <%=id %>Plugin;
