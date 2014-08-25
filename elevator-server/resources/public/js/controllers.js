var renderApp = angular.module('renderApp', ['stateService']);

renderApp.controller('RenderController', ['$scope', '$timeout', 'GameState', '$q', function ($scope, $timeout, GameState, $q) {
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