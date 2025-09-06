#!/bin/bash

# Build script for creating Keycloak theme manually
# This script creates the proper Keycloak theme structure without relying on keycloakify

set -e

echo "🔧 Building Congen Keycloak Account Theme..."

# Clean previous builds
rm -rf dist_keycloak
rm -rf theme_build

# Create the theme directory structure
mkdir -p theme_build/congen-account-theme/account/resources
mkdir -p theme_build/congen-account-theme/account/messages

# Build the webpack bundle first
echo "📦 Building webpack bundle..."
npm run build

# Copy webpack build output to theme resources
echo "📋 Copying webpack output to theme resources..."
cp -r dist/* theme_build/congen-account-theme/account/resources/

# Create theme.properties
echo "📝 Creating theme.properties..."
cat > theme_build/congen-account-theme/theme.properties << EOF
# Congen Account Theme Properties
parent=base
import=common/keycloak

# Account theme specific properties
stylesCommon=web_modules/@patternfly/react-core/dist/styles/base.css web_modules/@patternfly/react-core/dist/styles/app.css node_modules/patternfly/dist/css/patternfly.min.css node_modules/patternfly/dist/css/patternfly-additions.min.css
styles=css/account.css

# Enable localization
locales=en

# Custom properties for Congen theme
congen.brand.name=Congen
congen.brand.logo=/resources/img/congen-logo.png
EOF

# Create the main account page template
echo "🎨 Creating FreeMarker templates..."
mkdir -p theme_build/congen-account-theme/account
cat > theme_build/congen-account-theme/account/account.ftl << 'EOF'
<#import "template.ftl" as layout>
<@layout.mainLayout active='account' bodyClass='user'; section>

    <div class="row">
        <div class="col-md-10">
            <h2>${msg("editAccountHtmlTitle")}</h2>
        </div>
        <div class="col-md-2 subtitle">
            <span class="subtitle"><span class="required">*</span> ${msg("requiredFields")}</span>
        </div>
    </div>

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
                current: "${locale.current}",
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

    <script src="${url.resourcesPath}/js/runtime.e234dd3d.js"></script>
    <script src="${url.resourcesPath}/js/react.1395f85c.js"></script>
    <script src="${url.resourcesPath}/js/mui.5339071a.js"></script>
    <script src="${url.resourcesPath}/js/vendors.03fa96d6.js"></script>
    <script src="${url.resourcesPath}/js/keycloak-account-theme.9583ee9e.js"></script>

</@layout.mainLayout>
EOF

# Create template.ftl
cat > theme_build/congen-account-theme/account/template.ftl << 'EOF'
<#macro mainLayout active bodyClass>
<!DOCTYPE html>
<html class="${properties.kcHtmlClass!}" <#if realm.internationalizationEnabled??>lang="${locale.current}"</#if>>

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

<body class="admin-console user ${bodyClass}">

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
                            <div class="col-md-12">
                                <#nested "main">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

</body>
</html>
</#macro>
EOF

# Create messages directory and default messages
cat > theme_build/congen-account-theme/account/messages/messages_en.properties << 'EOF'
# Congen Account Theme Messages
accountManagementTitle=Congen Account Management
editAccountHtmlTitle=Edit Account
requiredFields=Required fields
EOF

# Create account.css
mkdir -p theme_build/congen-account-theme/account/resources/css
cat > theme_build/congen-account-theme/account/resources/css/account.css << 'EOF'
/* Congen Account Theme Styles */
body {
    font-family: 'Inter', 'system-ui', 'sans-serif';
    background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
    min-height: 100vh;
}

.navbar-title {
    color: #0ea5e9;
    font-weight: 600;
    font-size: 1.5rem;
}

.card-pf {
    border-radius: 16px;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1), 0 2px 4px rgba(0, 0, 0, 0.06);
    border: 1px solid #e5e5e5;
    background: #ffffff;
}

#kc-account-root {
    min-height: 400px;
    padding: 2rem;
}

/* Custom styling for React components */
.MuiButton-root {
    border-radius: 12px !important;
    text-transform: none !important;
    font-weight: 600 !important;
    padding: 12px 24px !important;
}

.MuiCard-root {
    border-radius: 16px !important;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1) !important;
}
EOF

# Create JAR file
echo "📦 Creating theme JAR file..."
mkdir -p dist_keycloak
cd theme_build
zip -r ../dist_keycloak/congen-account-theme.jar ./*
cd ..

# Create deployment structure
mkdir -p dist_keycloak/themes
cp -r theme_build/* dist_keycloak/themes/

echo "✅ Theme build completed!"
echo "📁 Theme files available in:"
echo "   - JAR: dist_keycloak/congen-account-theme.jar"
echo "   - Directory: dist_keycloak/themes/"
echo ""
echo "🚀 Ready for Keycloak deployment!"
