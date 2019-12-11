(function() {
  'use strict';

  angular
    .module('app')
    .filter('real', real);

  function real() {
    return function(valor) {

      return "R$ " + valor.toFixed(2).replace(/./g, function(c, i, a) {
        return i > 0 && c !== "." && (a.length - i) % 3 === 0 ? "," + c : c;
      }).split(".").join("x").split(",").join(".").split("x").join(",");
    };
  }


})();
