(function () {

  'use strict';

  angular.module('app').directive('listForms', function () {
    return {
      templateUrl: '/public/templates/listForms.html',
      scope: {
        formsCollection: "=ngModel",
        onDetailCallBack: '=', 
        essencialView: '=', 
        isSelectable: '=', 
      },
      link: function (scope) {
       scope.onDetailClick = function (param){
           if(scope.onDetailCallBack) scope.onDetailCallBack(param);
       }; 
         
      } ,
      controller: ['$scope','FormService','$filter',function($scope,FormService,$filter){
       // var teste = scope;
        $scope.prepareFormList = function (){
            for (var index = 0; index < $scope.formsCollection.length; index++) {
                    var form = $scope.formsCollection[index];                   
                    if(form.primaryFormSummary && (form.primaryFormSummary.dataType == 'date' || form.primaryFormSummary.dataType == 'month' )){
                        var dataValue = FormService.tryParseDate(form.primaryFormSummary.value);
                        if(form.primaryFormSummary.dataType == 'month'){
                            dataValue = $filter('date')(dataValue,'MM/yyyy'); 
                        }
                        else{
                            dataValue = $filter('date')(dataValue,'dd/MM/yyyy'); 
                        }
                        
                        form.primaryFormSummary.value =  dataValue;                        
                        $scope.formsCollection[index] =  form;                                                
                    } 
                    if(form.secondaryFormSummary && (form.secondaryFormSummary.dataType == 'date' || form.secondaryFormSummary.dataType == 'month' )){
                        var dataValue = FormService.tryParseDate(form.secondaryFormSummary.value);
                        if(form.secondaryFormSummary.dataType == 'month'){
                            dataValue = $filter('date')(dataValue,'MM/yyyy'); 
                        }
                        else{
                            dataValue = $filter('date')(dataValue,'dd/MM/yyyy'); 
                        }
                        
                        form.secondaryFormSummary.value = dataValue;                        
                        $scope.formsCollection[index] =  form;                                                
                    }                                     
                }
        }; 

        $scope.prepareFormList(); 
      } ]  
    };
  });

})();
