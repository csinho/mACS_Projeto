
angular.module("app").directive('validNumber', function() {
  return {
    require: '?ngModel',
    link: function(scope, element, attrs, ngModelCtrl) {
      if(!ngModelCtrl) {
        return;
      }

      ngModelCtrl.$parsers.push(function(val) {
        if (angular.isUndefined(val)) {
            val = '';
        }
        var clean = val.replace( /[^0-9]+/g, '');
        if (val !== clean) {
          ngModelCtrl.$setViewValue(clean);
          ngModelCtrl.$render();
        }
        return clean;
      });

      element.bind('keypress', function(event) {
        if(event.keyCode === 32) {
          event.preventDefault();
        }
      });
    }
  };
});

angular.module("app").directive('focusOn', function() {
  return function(scope, elem, attr) {
    scope.$on('focusOn', function(e, name) {
      if(name === attr.focusOn) {
        elem[0].focus();
      }
    });
  };
});

angular.module("app").factory('focus', function ($rootScope, $timeout) {
  return function(name) {
    $timeout(function (){
      $rootScope.$broadcast('focusOn', name);
    });
  };
});

angular.module("app").directive('errSrc', function() {
  return {
    link: function(scope, element, attrs) {
      element.bind('error', function() {
        if (attrs.src != attrs.errSrc) {
          attrs.$set('src', attrs.errSrc);
        }
      });
    }
  }
});

angular.module("app").directive('format', ['$filter',
 function($filter) {
   return {
     require: '?ngModel',
     link: function(scope, elem, attrs, ctrl) {
       if (!ctrl) return;

       ctrl.$formatters.unshift(function(a) {
         elem[0].value = ctrl.$modelValue;
         elem.priceFormat({
           prefix: '',
           centsSeparator: ',',
           thousandsSeparator: '.'
         });
         return elem[0].value;
       });

       ctrl.$parsers.unshift(function(viewValue) {
         elem.priceFormat({
           prefix: '',
           centsSeparator: ',',
           thousandsSeparator: '.'
           });
           return elem[0].value;
         });
       }
     };
   }
 ]);

angular.module("app").directive('resize', function ($window) {
    return function (scope, element, attr) {

        var w = angular.element($window);
        scope.$watch(function () {
            return {
                'h': window.innerHeight,
                'w': window.innerWidth
            };
        }, function (newValue, oldValue) {
            scope.windowHeight = newValue.h;
            scope.windowWidth = newValue.w;

            scope.resizeWithOffset = function (offsetH) {
                scope.$eval(attr.notifier);
                return {
                    'height': (newValue.h - offsetH) + 'px'
                };
            };

        }, true);

        w.bind('resize', function () {
            scope.$apply();
        });
    }
});

angular.module("app").directive("dragDrop", ["$parse",
  function($parse) {
    var sourceParent = "";
    var sourceIndex = -1;
    return {
      link: function($scope, elm, attr, ctrl) {
        // #region Initialization

        // Get TBODY of a element
        var tbody = elm.parent();
        // Set draggable true
        elm.attr("draggable", true);
        // If id of TBODY of current element already set then it won't set again
        tbody.attr('drag-id') ? void 0 : tbody.attr("drag-id", $scope.$id);
        // This add drag pointer
        elm.css("cursor", "move");

        // Events of element :- dragstart | dragover | drop | dragend
        elm.on("dragstart", onDragStart);
        elm.on("dragover", onDragOver);
        elm.on("drop", onDrop);
        elm.on("dragend", onDragEnd);

        // #endregion

        // This will trigger when user pick e row
        function onDragStart(e) {

          //Mozilla Hack
          //e.dataTransfer.setData("Text", "");

          if (e.dataTransfer) {
              e.dataTransfer.setData('text', "");
          }
          else if (e.originalEvent.dataTransfer){
              e.originalEvent.dataTransfer.setData('text', "");
          }

          if (!sourceParent) {
            // Set selected element's parent id
            sourceParent = tbody.attr('drag-id') ? tbody.attr('drag-id') : void 0;
            // Set selected element's index
            sourceIndex = $scope.$index;

            // This don't support in IE but other browser support it
            // This will set drag Image with it's position
            // IE automically set image by himself
            if (e.dataTransfer) {
              typeof e.dataTransfer.setDragImage !== "undefined" ?
                e.dataTransfer.setDragImage(e.target, -10, -10) : void 0;
            }
            else if (e.originalEvent.dataTransfer){
              typeof e.originalEvent.dataTransfer.setDragImage !== "undefined" ?
                e.originalEvent.dataTransfer.setDragImage(e.target, -10, -10) : void 0;
            }

            // This element will only drop to the element whose have drop effect 'move'
            if (e.dataTransfer) {
              e.dataTransfer.effectAllowed = 'move';
            }
            else if (e.originalEvent.dataTransfer){
              e.originalEvent.dataTransfer.effectAllowed = 'move';
            }
          }
          return true;
        }

        // This will trigger when user drag source element on another element
        function onDragOver(e) {

          // Prevent Default actions
          e.preventDefault ? e.preventDefault() : void 0;
          e.stopPropagation ? e.stopPropagation() : void 0;

          // This get current elements parent id
          var targetParent = tbody.attr('drag-id') ? tbody.attr('drag-id') : void 0;

          // If user drag elemnt from its boundary then cursor will show block icon else it will show move icon [ i.e : this effect work perfectly in google chrome]
          if (e.dataTransfer) {
            e.dataTransfer.dropEffect = sourceParent !== targetParent || typeof attr.ngRepeat === "undefined" ? 'none' : 'move';
          }
          else if (e.originalEvent.dataTransfer){
            e.originalEvent.dataTransfer.dropEffect = sourceParent !== targetParent || typeof attr.ngRepeat === "undefined" ? 'none' : 'move';
          }

          return false;
        }

        //This will Trigger when user drop source element on target element
        function onDrop(e) {

          // Prevent Default actions
          e.preventDefault ? e.preventDefault() : void 0;
          e.stopPropagation ? e.stopPropagation() : void 0;

          if (typeof attr.ngRepeat === "undefined")
            return false;
          // Get this item List
          var itemList = $parse(attr.ngRepeat.split("in")[1].trim())($scope);


          // Get target element's index
          var targetIndex = $scope.$index;

          // Get target element's parent id
          var targetParent = tbody.attr('drag-id') ? tbody.attr('drag-id') : void 0;

          // Get properties names which will be changed during the drag and drop
          var elements = attr.dragDrop ? attr.dragDrop.trim().split(",") : void 0;

          // If user dropped element into it's boundary and on another source not himself
          if (sourceIndex !== targetIndex && targetParent === sourceParent) {

            // If user provide element list by ','
            typeof elements !== "undefined" ? elements.forEach(function(element) {
              element = element.trim();
              typeof itemList[targetIndex][element] !== "undefined" ?
                itemList[targetIndex][element] = [itemList[sourceIndex][element], itemList[sourceIndex][element] = itemList[targetIndex][element]][0] : void 0;
            }) : void 0;
            // Visual row change
            itemList[targetIndex] = [itemList[sourceIndex], itemList[sourceIndex] = itemList[targetIndex]][0];
            // After completing the task directive send changes to the controller
            $scope.$apply(function() {
              typeof attr.afterDrop != "undefined" ?
                $parse(attr.afterDrop)($scope)({
                  sourceIndex: sourceIndex,
                  sourceItem: itemList[targetIndex],
                  targetIndex: targetIndex,
                  targetItem: itemList[sourceIndex]
                }) : void 0;
            });
          }
        }
        // This will trigger after drag and drop complete
        function onDragEnd(e) {
          //clearing the source
          sourceParent = "";
          sourceIndex = -1;
        }
      }
    }
  }
]);
