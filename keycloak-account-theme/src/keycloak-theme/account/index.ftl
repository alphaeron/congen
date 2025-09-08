<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" <#if realm.internationalizationEnabled??>lang="${locale}"</#if>>
<head>
    <meta charset="utf-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta name="robots" content="noindex, nofollow">
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <title>${msg("accountManagementTitle")}</title>
    <link rel="icon" href="${url.resourcesPath}/favicon.ico" />

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
    <div id="kc-account-root"></div>

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
            pageId: "account.ftl",
            themeType: "account",
            themeName: "congen-account-theme"
        };
    </script>

    <#-- Scripts will be automatically injected by Keycloakify -->
</body>
</html>