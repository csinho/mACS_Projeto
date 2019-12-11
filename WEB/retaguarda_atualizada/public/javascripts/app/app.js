(function() {
  'use strict';

  var matriculaLogado = '';

  var browserOk = (bowser.chrome && bowser.version >= 28) || (bowser.msie &&
    bowser.version >= 11) || (bowser.firefox && bowser.version >= 31) || (
    bowser.safari && bowser.version >= 10) || (bowser.ios);

  if (!browserOk) {
    $(document).ready(function() {
      document.getElementById("main").className += " hide";
      document.getElementById("navegadores-suportados").className =
        document.getElementById("navegadores-suportados").className.replace(
          'hide', '');
    });
  }

  angular
    .module('app', [
      'ngResource',
      'ngAnimate',
      'ui.utils',
      'ui.utils.masks',
      'idf.br-filters',
      'prodeb-components',
      'angularjs-dropdown-multiselect',
      'ui.bootstrap',
      'angucomplete-alt',
      'monospaced.qrcode',
      'ngImgCrop'
    ]);

  angular
    .module('app')
    .config(config);

  config.$inject = ['$httpProvider'];

  function config($httpProvider) { $httpProvider.interceptors.push('SpinnerInterceptor');}

  angular.module('app').factory('SpinnerInterceptor', ['Spinner', '$q',function(Spinner, $q) {  
    var spinnerInterceptor = {
        request: function(request) {  Spinner.show("Aguarde...");	return request;},
        requestError: function(request) {  Spinner.hide();	return $q.reject(request);},
        response: function(response) {  Spinner.hide();	return response;},
        responseError: function(response) {  Spinner.hide();	return $q.reject(response);},
    };
    return spinnerInterceptor;
  }]);

})();

