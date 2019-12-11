(function() {
  'use strict';

  angular
    .module('app')
    .filter('joinArrayKey', joinArrayKey);

  function joinArrayKey() {
    return function(array, key) {
      if(!array) return '';

      return array.map(function(elem){
        return elem[key];
      }).join(", ");
      
    };
  }
})();
