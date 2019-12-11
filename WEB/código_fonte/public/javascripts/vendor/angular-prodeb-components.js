(function() {
  'use strict';

  angular.module('prodeb-components', []);

  /* PAGINACAO */

  angular.module('prodeb-components')
    .factory('Paginacao', paginacaoService)
    .directive('paginacao', paginacaoDirective)
    .factory('Spinner', spinnerService)
    .directive('spinner', spinnerDirective)
    .directive('allowPattern', allowPatternDirective)
    .directive('datePicker',['$timeout',datePickerDirective])
    .directive('emptyToNull',emptyToNullDirective);

  paginacaoService.$inject = [];

  function paginacaoService() {

    var paginacao = {};

    paginacao.getPaginador = function (metodoDeBusca, quantidadePorPagina, _options) {
        var porPagina = quantidadePorPagina === undefined ? 10 : quantidadePorPagina;

        var options = {
            quantidadePaginasDoSeletor: 10,
            labels: { primeira: '«« Primeira', anterior: '« Anterior', proxima: 'Próxima »', ultima: 'Última »»' }
        }

        if (_options !== undefined) {
            if (_options.quantidadePaginasDoSeletor !== undefined) {
                options.quantidadePaginasDoSeletor = _options.quantidadePaginasDoSeletor;
            }
            if (_options.labels !== undefined) options.labels = _options.labels;
        }

        options.quantidadePaginasDoSeletorIntermediario = Math.floor(options.quantidadePaginasDoSeletor / 2);

        var paginador = {
            metodoDeBusca: metodoDeBusca,
            numeroPaginas: 1,
            total: 0,
            porPagina: porPagina,
            paginaAtual: 0,
            options: options
        };

        paginador.calcularNumeroPaginas = function (total) {
            this.total = total;
            if (total <= 0)
                this.numeroPaginas = 1
            else
                this.numeroPaginas = Math.floor(total / this.porPagina) + (total % this.porPagina > 0 ? 1 : 0);
        };

        paginador.paginas = function () {
            var ret = [];

            for (var i = 1; i <= paginador.numeroPaginas ; i++) {
                if (paginador.paginaAtual == i) {
                    ret.push(i);
                } else {
                    if (paginador.paginaAtual <= (paginador.options.quantidadePaginasDoSeletorIntermediario + 1)) {
                        if (i <= paginador.options.quantidadePaginasDoSeletor) ret.push(i);
                    } else {
                        if ((i >= paginador.paginaAtual - paginador.options.quantidadePaginasDoSeletorIntermediario) && (i <= paginador.paginaAtual + paginador.options.quantidadePaginasDoSeletorIntermediario)) ret.push(i);
                    }
                }
            }
            return ret;
        };

        paginador.paginaAnterior = function () {
            if (paginador.paginaAtual > 1) {
                paginador.metodoDeBusca(paginador.paginaAtual - 1);
            }
        };

        paginador.proximaPagina = function () {
            if (paginador.paginaAtual < paginador.numeroPaginas) {
                paginador.metodoDeBusca(paginador.paginaAtual + 1);
            }
        };

        paginador.irParaPagina = function (pagina) {
            if (pagina >= 1 && pagina <= paginador.numeroPaginas) {
                paginador.metodoDeBusca(pagina);
            }
        };

        return paginador;
    }

    return paginacao;
  }

  paginacaoDirective.$inject = [];

  function paginacaoDirective() {
    return {
        restrict: 'AE',
        scope: {
            paginador: '='
        },
        templateUrl: '/public/templates/libs/paginacao.html'
    };
  }

  /* SPINNER */

  spinnerService.$inject = ['$rootScope'];

  function spinnerService($rootScope) {
    return {
      show: show,
      hide: hide
    };

    function show(message) {
      $rootScope.$broadcast('show-spinner', { message: message });
    }

    function hide() {
      $rootScope.$broadcast('hide-spinner');
    }
  }

  spinnerDirective.$inject = [];

  function spinnerDirective() {
    return {
        restrict: 'E',
        templateUrl: '/public/templates/libs/spinner.html',
        link: function(scope, elem, attrs) {
          scope.spinner = { show: false, message: "" }

          scope.$on('show-spinner', function(event, args) {
            scope.spinner = { show: true, message: args.message }
          });

          scope.$on('hide-spinner', function(event, args) {
            scope.spinner = { show: false, message: "" }
          });
        }
    };
  }
  
  allowPatternDirective.$inject = [];
  
  //by: Sean Larkin
  function allowPatternDirective() {
	    return {
	        restrict: "A",
	        compile: function(tElement, tAttrs) {
	            return function(scope, element, attrs) {
	            	// I handle key events
	                element.bind("keypress", function(event) {
	                    var keyCode = event.which || event.keyCode; // I safely get the keyCode pressed from the event.
	                    var keyCodeChar = String.fromCharCode(keyCode); // I determine the char from the keyCode.
	                   
                        if( keyCode === 8 || ( keyCode >= 35 && keyCode <= 46)  ) {
                            return true;
                        }
	                    // If the keyCode char does not match the allowed Regex Pattern, then don't allow the input into the field.
	                    if (!keyCodeChar.match(new RegExp(attrs.allowPattern, "i"))) {
	                    	event.preventDefault();
	                        return false;
	                    }
	          
	                });
	            };
	        }
	    };
	}
  
  datePickerDirective.$inject = [];
  
  function datePickerDirective($timeout){
	  
	  return{
		  restrict: "A",
		  link: function(scope, elem, attrs) {
			  $(elem).datepicker({
	            changeYear: true,
	            changeMonth: true,
	            yearRange: '2010:2050',
	            dateFormat: 'dd/mm/yy',
	            dayNames: ['Domingo','Segunda','Terça','Quarta','Quinta','Sexta','Sábado'],
	            dayNamesMin: ['D','S','T','Q','Q','S','S','D'],
	            dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb','Dom'],
	            monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
	            monthNamesShort: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho','Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
	            nextText: 'Próximo',
	            prevText: 'Anterior'
		  });
	   }
     }
  }
  
  emptyToNullDirective.$inject = [];
  
  function emptyToNullDirective(){
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function (scope, elem, attrs, ctrl) {
          ctrl.$parsers.push(function(viewValue) {
              if(viewValue === "") {
                  return null;
              }
              return viewValue;
          });
      }
    };
  }
  
})();
