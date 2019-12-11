(function(){   
  angular.module("app").controller("QRCodesController", QRCodesController);
  
  function QRCodesController($http, $window){
    var me = this;
    me.generate = generate;
    
    me.txtNumQR = 100;
    
    me.version = 2;
    me.errorCorrectionLevel = 'M';
    me.size = 200;
    me.data = [];
    me.print = print;
     
    /*
     * Generate a list of qrcode numbers to render on screen
     */
    function doGenerate(start){
      var qtd = parseInt(me.txtNumQR, 10);
      for(var i = 0; i < qtd; i++){
        me.data.push(start++);
      }
    }
    
    /*
     * Generate a list of qrcode numbers based on the server
     * timestamp as a prefix.
     */
    function generate(){
      me.data = [];
      
      // It's actually preferable to ask the server for the timestamp.
      // This ensures an unique prefix for the qrcodes
      $http.get('/sync/time').success(function(time){
        //var start = new Date().getTime() * 1000;
        var start = parseInt(time, 10) * 1000;
        doGenerate(start);
      });
    }
    
    /**
     * Prints the current form detail page
     */
    function print() {
      $window.print();
    }
  }
})();