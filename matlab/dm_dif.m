dmdif = zeros(12, size(y, 2));
dmdifp = zeros(12, size(y, 2));

for k=1:size(dmdif, 2)
    dmdif(1, k) = dmtest(pe(:, k, 49), pe(:, k, 58));
    dmdif(2, k) = dmtest(pe(:, k, 50), pe(:, k, 59));
    dmdif(3, k) = dmtest(pe(:, k, 51), pe(:, k, 60));
    dmdif(4, k) = dmtest(pe(:, k, 49), pe(:, k, 61));
    dmdif(5, k) = dmtest(pe(:, k, 50), pe(:, k, 62));
    dmdif(6, k) = dmtest(pe(:, k, 51), pe(:, k, 63));
    dmdif(7, k) = dmtest(pe(:, k, 49), pe(:, k, 64));
    dmdif(8, k) = dmtest(pe(:, k, 50), pe(:, k, 65));
    dmdif(9, k) = dmtest(pe(:, k, 51), pe(:, k, 66));
    dmdif(10, k) = dmtest(pe(:, k, 49), pe(:, k, 67));
    dmdif(11, k) = dmtest(pe(:, k, 50), pe(:, k, 68));
    dmdif(12, k) = dmtest(pe(:, k, 51), pe(:, k, 69));
end

for i=1:size(dmdifp, 1)
   for j=1:size(dmdifp, 2)
       dmdifp(i, j) = normcdf(dmdif(i, j));
   end
end