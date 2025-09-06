"use strict";
(globalThis["webpackChunkcongen_keycloak_account_theme"] = globalThis["webpackChunkcongen_keycloak_account_theme"] || []).push([["src_account_KcPage_tsx"],{

/***/ "./src/account/KcPage.tsx":
/*!********************************!*\
  !*** ./src/account/KcPage.tsx ***!
  \********************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   "default": () => (/* binding */ KcPage)
/* harmony export */ });
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! react */ "./node_modules/react/index.js");
/* harmony import */ var react__WEBPACK_IMPORTED_MODULE_0___default = /*#__PURE__*/__webpack_require__.n(react__WEBPACK_IMPORTED_MODULE_0__);
/* harmony import */ var _i18n__WEBPACK_IMPORTED_MODULE_1__ = __webpack_require__(/*! ./i18n */ "./src/account/i18n.ts");
/* harmony import */ var keycloakify_account_DefaultPage__WEBPACK_IMPORTED_MODULE_2__ = __webpack_require__(/*! keycloakify/account/DefaultPage */ "./node_modules/keycloakify/account/DefaultPage.js");
/* harmony import */ var keycloakify_account_Template__WEBPACK_IMPORTED_MODULE_3__ = __webpack_require__(/*! keycloakify/account/Template */ "./node_modules/keycloakify/account/Template.js");




function KcPage(props) {
  const {
    kcContext
  } = props;
  const {
    i18n
  } = (0,_i18n__WEBPACK_IMPORTED_MODULE_1__.useI18n)({
    kcContext
  });
  return /*#__PURE__*/React.createElement(react__WEBPACK_IMPORTED_MODULE_0__.Suspense, null, (() => {
    switch (kcContext.pageId) {
      default:
        return /*#__PURE__*/React.createElement(keycloakify_account_DefaultPage__WEBPACK_IMPORTED_MODULE_2__["default"], {
          kcContext: kcContext,
          i18n: i18n,
          classes: classes,
          Template: keycloakify_account_Template__WEBPACK_IMPORTED_MODULE_3__["default"],
          doUseDefaultCss: true
        });
    }
  })());
}
const classes = {};

/***/ }),

/***/ "./src/account/i18n.ts":
/*!*****************************!*\
  !*** ./src/account/i18n.ts ***!
  \*****************************/
/***/ ((__unused_webpack_module, __webpack_exports__, __webpack_require__) => {

__webpack_require__.r(__webpack_exports__);
/* harmony export */ __webpack_require__.d(__webpack_exports__, {
/* harmony export */   useI18n: () => (/* binding */ useI18n)
/* harmony export */ });
/* harmony import */ var keycloakify_account__WEBPACK_IMPORTED_MODULE_0__ = __webpack_require__(/*! keycloakify/account */ "./node_modules/keycloakify/account/index.js");
/* eslint-disable @typescript-eslint/no-unused-vars */


/** @see: https://docs.keycloakify.dev/features/i18n */
const {
  useI18n,
  ofTypeI18n
} = keycloakify_account__WEBPACK_IMPORTED_MODULE_0__.i18nBuilder.withThemeName().build();


/***/ })

}]);
//# sourceMappingURL=src_account_KcPage_tsx.map