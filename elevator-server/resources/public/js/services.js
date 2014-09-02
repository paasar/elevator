var stateService = angular.module('stateService', ['ngResource']);

stateService.factory('GameState', ['$resource',
    function($resource) {
        return $resource('/state', {}, {
            query: {method:'GET', params:{}, isArray:true}
        });
    }]);

stateService.factory('GameInternalState', ['$resource',
    function($resource) {
        return $resource('/state/admin', {}, {
            query: {method:'GET', params:{}}
        });
    }]);