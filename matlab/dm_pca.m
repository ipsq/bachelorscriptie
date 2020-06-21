dmpca = zeros(25, size(y, 2));
dmpcap = zeros(25, size(y, 2));

for k=1:size(dmpca, 2)
    dmpca(1, k) = dmtest(pe(:, k, 3), pe(:, k, 5));
    dmpca(2, k) = dmtest(pe(:, k, 4), pe(:, k, 6));
    dmpca(3, k) = dmtest(pe(:, k, 9), pe(:, k, 11));
    dmpca(4, k) = dmtest(pe(:, k, 10), pe(:, k, 12));
    dmpca(5, k) = dmtest(pe(:, k, 19), pe(:, k, 31));
    dmpca(6, k) = dmtest(pe(:, k, 20), pe(:, k, 32));
    dmpca(7, k) = dmtest(pe(:, k, 21), pe(:, k, 33));
    dmpca(8, k) = dmtest(pe(:, k, 22), pe(:, k, 34));
    dmpca(9, k) = dmtest(pe(:, k, 23), pe(:, k, 35));
    dmpca(10, k) = dmtest(pe(:, k, 24), pe(:, k, 36));
    dmpca(11, k) = dmtest(pe(:, k, 25), pe(:, k, 37));
    dmpca(12, k) = dmtest(pe(:, k, 26), pe(:, k, 38));
    dmpca(13, k) = dmtest(pe(:, k, 27), pe(:, k, 39));
    dmpca(14, k) = dmtest(pe(:, k, 28), pe(:, k, 40));
    dmpca(15, k) = dmtest(pe(:, k, 29), pe(:, k, 41));
    dmpca(16, k) = dmtest(pe(:, k, 30), pe(:, k, 42));
    dmpca(17, k) = dmtest(pe(:, k, 52), pe(:, k, 55));
    dmpca(18, k) = dmtest(pe(:, k, 53), pe(:, k, 56));
    dmpca(19, k) = dmtest(pe(:, k, 54), pe(:, k, 57));
    dmpca(20, k) = dmtest(pe(:, k, 58), pe(:, k, 64));
    dmpca(21, k) = dmtest(pe(:, k, 59), pe(:, k, 65));
    dmpca(22, k) = dmtest(pe(:, k, 60), pe(:, k, 66));
    dmpca(23, k) = dmtest(pe(:, k, 61), pe(:, k, 67));
    dmpca(24, k) = dmtest(pe(:, k, 62), pe(:, k, 68));
    dmpca(25, k) = dmtest(pe(:, k, 63), pe(:, k, 69));
end

for i=1:size(dmpcap, 1)
   for j=1:size(dmpcap, 2)
       dmpcap(i, j) = normcdf(dmpca(i, j));
   end
end