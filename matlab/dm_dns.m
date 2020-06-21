dmdns = zeros(18, size(y, 2));
dmdnsp = zeros(18, size(y, 2));

for k=1:size(dmdns, 2)
    dmdns(1, k) = dmtest(pe(:, k, 13), pe(:, k, 16));
    dmdns(2, k) = dmtest(pe(:, k, 14), pe(:, k, 17));
    dmdns(3, k) = dmtest(pe(:, k, 15), pe(:, k, 18));
    dmdns(4, k) = dmtest(pe(:, k, 19), pe(:, k, 22));
    dmdns(5, k) = dmtest(pe(:, k, 20), pe(:, k, 23));
    dmdns(6, k) = dmtest(pe(:, k, 21), pe(:, k, 24));
    dmdns(7, k) = dmtest(pe(:, k, 25), pe(:, k, 28));
    dmdns(8, k) = dmtest(pe(:, k, 26), pe(:, k, 29));
    dmdns(9, k) = dmtest(pe(:, k, 27), pe(:, k, 30));
    dmdns(10, k) = dmtest(pe(:, k, 31), pe(:, k, 34));
    dmdns(11, k) = dmtest(pe(:, k, 32), pe(:, k, 35));
    dmdns(12, k) = dmtest(pe(:, k, 33), pe(:, k, 36));
    dmdns(13, k) = dmtest(pe(:, k, 37), pe(:, k, 40));
    dmdns(14, k) = dmtest(pe(:, k, 38), pe(:, k, 41));
    dmdns(15, k) = dmtest(pe(:, k, 39), pe(:, k, 42));
    dmdns(16, k) = dmtest(pe(:, k, 43), pe(:, k, 46));
    dmdns(17, k) = dmtest(pe(:, k, 44), pe(:, k, 47));
    dmdns(18, k) = dmtest(pe(:, k, 45), pe(:, k, 48));
end

for i=1:size(dmdnsp, 1)
   for j=1:size(dmdnsp, 2)
       dmdnsp(i, j) = normcdf(dmdns(i, j));
   end
end