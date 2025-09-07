<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" <#if realm.internationalizationEnabled??>lang="${locale}"</#if>>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${msg("accountManagementTitle")}</title>
    <link rel="icon" href="${url.resourcesPath}/favicon-32x32.png" />

    <#if properties.stylesCommon?has_content>
        <#list properties.stylesCommon?split(' ') as style>
            <link href="${url.resourcesCommonPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>
    
    <#if properties.styles?has_content>
        <#list properties.styles?split(' ') as style>
            <link href="${url.resourcesPath}/${style}" rel="stylesheet" />
        </#list>
    </#if>

    <#if properties.scripts?has_content>
        <#list properties.scripts?split(' ') as script>
            <script src="${url.resourcesPath}/${script}" type="text/javascript"></script>
        </#list>
    </#if>
</head>

<body class="admin-console user">
    <div class="container-fluid">
        <div class="row">
            <div class="col-sm-10 col-sm-offset-1 col-md-8 col-md-offset-2 col-lg-8 col-lg-offset-2">
                <div class="card-pf">
                    <header class="navbar navbar-default navbar-pf" role="banner">
                        <div class="navbar-header">
                            <div class="container-fluid">
                                <h1 class="navbar-title">Congen Account Management</h1>
                            </div>
                        </div>
                    </header>

                    <div class="container-fluid">
                        <div class="row">
                            <div class="col-md-10">
                                <h2>${msg("accountManagementTitle")}</h2>
                            </div>
                            <div class="col-md-2 subtitle">
                                <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-12">
                                <div id="kc-account-root"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        // Initialize React app with Keycloak context
        window.kcContext = {
            url: {
                resourcesPath: "${url.resourcesPath}",
                accountUrl: "${url.accountUrl}"
            },
            realm: {
                displayName: "${realm.displayName!""}",
                name: "${realm.name}",
                internationalizationEnabled: ${realm.internationalizationEnabled?c}
            },
            locale: {
                current: "${locale}",
                supported: [<#list locale.supported as l>"${l.label}"<#if l_has_next>,</#if></#list>]
            },
            user: {
                username: "${account.username!""}",
                email: "${account.email!""}",
                firstName: "${account.firstName!""}",
                lastName: "${account.lastName!""}",
                attributes: {
                    <#if account.attributes??>
                        <#list account.attributes?keys as key>
                            "${key}": [<#list account.attributes[key] as value>"${value}"<#if value_has_next>,</#if></#list>]<#if key_has_next>,</#if>
                        </#list>
                    </#if>
                }
            },
            message: {
                type: "${message.type!""}",
                summary: "${message.summary!""}"
            },
            pageId: "index.ftl",
            themeType: "account",
            themeName: "congen-account-theme"
        };
    </script>

    <script src="${url.resourcesPath}/js/runtime.f7745ae4.js"></script>
    <script src="${url.resourcesPath}/js/react.cd4bb30b.js"></script>
    <script src="${url.resourcesPath}/js/mui-19aa1551.08302dc9.js"></script>
    <script src="${url.resourcesPath}/js/vendors.95d5c4a4.js"></script>
    <script src="${url.resourcesPath}/js/keycloak-account-theme.cdc7c5d1.js"></script>
</body>
</html>