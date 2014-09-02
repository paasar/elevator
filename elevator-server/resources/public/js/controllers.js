var renderGameApp = angular.module('renderGameApp', ['stateService']);

renderGameApp.controller('GameRenderController', ['$scope', '$timeout', 'GameState', '$q', function ($scope, $timeout, GameState, $q) {
    $scope.states = GameState.query();

    var updateState = function () {
         var newStates = GameState.query();
        // Wait for the response to prevent blinking.
        $q.all([
            newStates.$promise
        ]).then(function() {
            $scope.states = newStates;
        });
        $timeout(function() {updateState();}, 1000);

    };
    $timeout(function() {updateState();}, 1000);
}]);

var renderAdminApp = angular.module('renderAdminApp', ['stateService']);

renderAdminApp.controller('AdminRenderController', ['$scope', '$timeout', 'GameInternalState', '$q', function ($scope, $timeout, GameInternalState, $q) {
    $scope.state = GameInternalState.query();

    var updateState = function () {
        var newState = GameInternalState.query();
        // Wait for the response to prevent blinking.
        $q.all([
            newState.$promise
        ]).then(function() {
            $scope.state = newState;
        });
        $timeout(function() {updateState();}, 1000);

    };
    $timeout(function() {updateState();}, 1000);
}]);