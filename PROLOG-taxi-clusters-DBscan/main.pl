import:-
    csv_read_file('partition65.csv', Data65, [functor(partition)]),maplist(assert, Data65),
    csv_read_file('partition74.csv', Data74, [functor(partition)]),maplist(assert, Data74),
    csv_read_file('partition75.csv', Data75, [functor(partition)]),maplist(assert, Data75),
    csv_read_file('partition76.csv', Data76, [functor(partition)]),maplist(assert, Data76),
    csv_read_file('partition84.csv', Data84, [functor(partition)]),maplist(assert, Data84),
    csv_read_file('partition85.csv', Data85, [functor(partition)]),maplist(assert, Data85),
    csv_read_file('partition86.csv', Data86, [functor(partition)]),maplist(assert, Data86),listing(partition).

% union/3
% adds an element to a list of elements
% union(cluster, clusterList, newClusterList)
union(A, B, [A|B]).

% clusterID/2
% gets the Cluster ID from a partition
% clusterID(ID, partition)
clusterID(_, []):-!.
clusterID(X,[_|T]) :- memberchk(_,T), clusterID(X, T).
clusterID(X, [X|T]):-clusterID(X,T).

% getClusterID/4
% gets two Cluster IDs from two partitions.
% getClusterID(ID1, ID2, partition1, partition2)
getClusterID(X, Y, L, M):-clusterID(X,L), clusterID(Y,M), !.

% tuneLabel/4
% relabels the points of cluster O with label R in a one dimensional list
% tuneLabel(O,R,clusterListIn, clusterListOut)
tuneLabel(_,_,[],[]):-!.
tuneLabel(X,Y,[H|T],[H|T1]):-  \+is_list(H), dif(X,H), tuneLabel(X,Y,T,T1), !.
tuneLabel(X,Y,[H|T],[H|T1]):- \+is_list(H), memberchk(_,T), tuneLabel(X,Y,T,T1), !.
tuneLabel(X,Y,[X|T],[Y|T1]):-  tuneLabel(X,Y,T,T1).
tuneLabel(X,Y,[H|T],[H1|T1]):- is_list(H),tuneLabel(X,Y,H,H1), tuneLabel(X,Y,T,T1), !.

% relabelList/3
% relabels partitions cluster ID with the cluster ID of another partition
% relabelList(cluster, clusterListIn, clusterListOut)
relabelList(A, [H|T], C):- getClusterID(X,Y, A, [H|T]), tuneLabel(Y, X, [H|T], C), !.

% intersect/3
% finds points in cluster list that intersects with specified partition, relabeling the points cluster ID with the specified partitions cluster ID in the cluster list.
% intersect(cluster, clusterList, relabeledClusterList).
intersect(_,[],[]):-!.
intersect([H|T], [H1|T1], K):- \+is_list(H1), H=:=H1, relabelList([H|T], [H1|T1], K), intersect([H|T], [], []).
intersect([H|T], [H1|T1], [H1|T1]):- \+is_list(H1), intersect([H|T], [], []).
intersect(A, [H|T], [H1|T1]):- is_list(H), intersect(A, H, H1), intersect(A, T, T1), !.

% empty/1
% checks that list is empty
% is_empty(List)
is_empty(L):- not(member(_,L)).

% clusterList/3
% obtains the cluster list from list of partitions
% clusterList(partitionList, builtClusterList, clusterList)
clusterList([], L, L):- !.
clusterList([H|T], B, L):- is_empty(B), clusterList(T, H, L), !.
clusterList([H|T], B, C) :- intersect(H, B, K), union(H, K, L), clusterList(T, L, C), !.

% mergeClusters/1
% obtains global list of clusters, using this list to find its clusterList
% mergeClusters(clusterList)
mergeClusters(L) :- findall([D,X,Y,C],partition(_,D,X,Y,C),A), clusterList(A, [], L), !.

test1(union) :- write('union([5, 6.5, 4, 6], [4.5], Result)'), nl, union([5, 6.5, 4, 6], [4.5], Result), write(Result).
test2(clusterID) :- write('clusterID(X, [45, 63, 3, 4, 6, 7])'), nl, clusterID(X, [45, 63, 3, 4, 6, 7]), write(X).
test3(getClusterID) :- write('getClusterID(X,Y, [2,1,6],[33,2.2,3.1,13])'), nl, getClusterID(X,Y, [2,11,6,6],[33,2.2,3.1,13]), write(X),nl, write(Y).
test4(tuneLabel) :- write('tuneLabel(13, 10000, [13,2.2,3.1,13], C)'), nl, tuneLabel(13, 10000, [13,2.2,3.1,13], C), write(C).
test5(relabelList):- write('relabelList([2, 3, 4], [2,7,5], C)'),nl, relabelList([2, 3, 4], [2,7,5], C), write(C).
test6(intersect) :- write('intersect([2, 3, 4], [[2, 2, 5], [3, 6, 5], [2, 8, 7]], K)'), nl, intersect([2, 3, 4], [[2, 2, 5], [3, 6, 5], [2, 8, 7]], K), write(K).
test7(is_empty) :- is_empty([]).
test8(clusterList):- write('clusterList([[5,2.2,3.1,33], [2,2.1,3.1,22], [5,2.5,3.1,43], [4,2.1,4.1,33], [5,4.1,3.1,30]], [], L)'),nl, clusterList([[5,2.2,3.1,33], [2,2.1,3.1,22], [5,2.5,3.1,43], [4,2.1,4.1,33], [5,4.1,3.1,30]], [], L), write(L).

% implementation of relabel following rubric specifications, isnt used in actual algorithmn
% relabel/4
% relabels the points of cluster O with label R in a two dimensional list
% relabel(O,R,clusterListIn, clusterListOut)
relabel(_,_,[],[]):-!.
relabel(X,Y,[H|T],[H|T1]):-  \+is_list(H), dif(X,H), relabel(X,Y,T,T1).
relabel(X,Y,[H|T],[H|T1]):- \+is_list(H), memberchk(_,T), relabel(X,Y,T,T1).
relabel(X,Y,[X|T],[Y|T1]):-  relabel(X,Y,T,T1).
relabel(X,Y,[H|T],[H1|T1]):- is_list(H),relabel(X,Y,H,H1), relabel(X,Y,T,T1), !.

test(relabel) :- write('relabel(33, 77,
[[1,2.2,3.1,33], [2,2.1,3.1,22], [3,2.5,3.1,33], [4,2.1,4.1,33],[5,4.1,3.1,30]],Result)'),nl,
relabel(33, 77,
[[1,2.2,3.1,33], [2,2.1,3.1,22], [3,2.5,3.1,33], [4,2.1,4.1,33], [5,4.1,3.1,30]],Result),
write(Result).
