yields = import_yields();
macro = import_macro();

yieldsNorm = normalize(yields.Variables);
macroNorm = normalize(macro.Variables);
pcaYieldsCoeff = pca(yieldsNorm);
pcaMacroCoeff = pca(macroNorm);
spcaMacroCoeff = spca(macroNorm, [], 3, inf, -[75, 50, 25]);

pcaYields = array2timetable(yieldsNorm*pcaYieldsCoeff, 'RowTimes', yields.date);
pcaMacro = array2timetable(macroNorm*pcaMacroCoeff, 'RowTimes', macro.date);
spcaMacro = array2timetable(macroNorm*spcaMacroCoeff, 'RowTimes', macro.date);

sample = timerange('1992-01-01', '1999-12-01', 'months');
% sample = timerange('2000-01-01', '2007-12-01', 'months');
% sample = timerange('2008-01-01', '2016-12-01', 'months');
% sample = timerange('1992-01-01', '2016-12-01', 'months');

cols = [1, 2, 3, 5, 10]; % tau = 1, 2, 3, 5, 10
y = yields(sample, cols);

nModels = 69;
forecasts = zeros(size(y, 1), size(y, 2), nModels);
dm = zeros(nModels - 1, size(y, 2));

parfor i=1:size(y,1)
    trainingSample = timerange(datestr(addtodate(datenum(y.date(i)), -120, 'month')), ...
        datestr(addtodate(datenum(y.date(i)), -1, 'month')), 'months'); % rolling window of 120 months
    % hack to fit models with lagged regressors
    exogenousSample = timerange(datestr(addtodate(datenum(y.date(i)), -121, 'month')), ...
        datestr(addtodate(datenum(y.date(i)), -1, 'month')), 'months'); % rolling window of 121 months
    
    trainingFullYields = yields(trainingSample, :).Variables;
    trainingYields = yields(trainingSample, cols).Variables;
    trainingMacro = macro(exogenousSample, {'CUMFNS', 'FEDFUNDS', 'INDPRO'}).Variables;
    trainingPcaMacro = pcaMacro(exogenousSample, :).Variables;
    trainingPcaYields = pcaYields(exogenousSample, :).Variables;
    trainingSpcaMacro = spcaMacro(exogenousSample, :).Variables;
    
    f = zeros(size(y, 2), nModels);
    f(:, 1) = ar_1(trainingYields);
    f(:, 2) = var_1(trainingYields);
    f(:, 3) = var_1_factors(trainingYields, trainingPcaMacro(:, 1));
    f(:, 4) = var_1_factors(trainingYields, trainingPcaMacro(:, 1:2));
    f(:, 5) = var_1_factors(trainingYields, trainingSpcaMacro(:, 1));
    f(:, 6) = var_1_factors(trainingYields, trainingSpcaMacro(:, 1:2));
    f(:, 7) = ar_sic(trainingYields);
    f(:, 8) = var_sic(trainingYields);
    f(:, 9) = var_sic_factors(trainingYields, trainingPcaMacro(:, 1));
    f(:, 10) = var_sic_factors(trainingYields, trainingPcaMacro(:, 1:2));
    f(:, 11) = var_sic_factors(trainingYields, trainingSpcaMacro(:, 1));
    f(:, 12) = var_sic_factors(trainingYields, trainingSpcaMacro(:, 1:2));
    f(:, 13) = dns_ar(trainingFullYields, [12 24 36 48 60 72 84 96 108 120]);
    f(:, 14) = dns_ar(trainingFullYields, [12 24 36 60 84 120]);
    f(:, 15) = dns_ar(trainingFullYields, [12 36 60 120]);
    f(:, 16) = dns_var(trainingFullYields, [12 24 36 48 60 72 84 96 108 120]);
    f(:, 17) = dns_var(trainingFullYields, [12 24 36 60 84 120]);
    f(:, 18) = dns_var(trainingFullYields, [12 36 60 120]);
    
    f(:, 19) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1));
    f(:, 20) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1));
    f(:, 21) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1));
    f(:, 22) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1));
    f(:, 23) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1));
    f(:, 24) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1));
    
    f(:, 25) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1:2));
    f(:, 26) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1:2));
    f(:, 27) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1:2));
    f(:, 28) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1:2));
    f(:, 29) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1:2));
    f(:, 30) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1:2));
    
    f(:, 31) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingSpcaMacro(:, 1));
    f(:, 32) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingSpcaMacro(:, 1));
    f(:, 33) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingSpcaMacro(:, 1));
    f(:, 34) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingSpcaMacro(:, 1));
    f(:, 35) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingSpcaMacro(:, 1));
    f(:, 36) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingSpcaMacro(:, 1));
    
    f(:, 37) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingSpcaMacro(:, 1:2));
    f(:, 38) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingSpcaMacro(:, 1:2));
    f(:, 39) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingSpcaMacro(:, 1:2));
    f(:, 40) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingSpcaMacro(:, 1:2));
    f(:, 41) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingSpcaMacro(:, 1:2));
    f(:, 42) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingSpcaMacro(:, 1:2));
    
    f(:, 43) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingMacro);
    f(:, 44) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingMacro);
    f(:, 45) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingMacro);
    f(:, 46) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingMacro);
    f(:, 47) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingMacro);
    f(:, 48) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingMacro);
    
    f(:, 49) = var_1_factors(trainingYields, trainingPcaYields(:, 1));
    f(:, 50) = var_1_factors(trainingYields, trainingPcaYields(:, 1:2));
    f(:, 51) = var_1_factors(trainingYields, trainingPcaYields(:, 1:3));
    f(:, 52) = var_1_factors(trainingYields, trainingPcaMacro(:, 1));
    f(:, 53) = var_1_factors(trainingYields, trainingPcaMacro(:, 1:2));
    f(:, 54) = var_1_factors(trainingYields, trainingPcaMacro(:, 1:3));
    f(:, 55) = var_1_factors(trainingYields, trainingSpcaMacro(:, 1));
    f(:, 56) = var_1_factors(trainingYields, trainingSpcaMacro(:, 1:2));
    f(:, 57) = var_1_factors(trainingYields, trainingSpcaMacro(:, 1:3));
    
    f(:, 58) = var_1_factors(trainingYields, [trainingPcaYields(:, 1) trainingPcaMacro(:, 1)]);
    f(:, 59) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:2) trainingPcaMacro(:, 1)]);
    f(:, 60) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:3) trainingPcaMacro(:, 1)]);
    f(:, 61) = var_1_factors(trainingYields, [trainingPcaYields(:, 1) trainingPcaMacro(:, 1:2)]);
    f(:, 62) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:2) trainingPcaMacro(:, 1:2)]);
    f(:, 63) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:3) trainingPcaMacro(:, 1:2)]);
    
    f(:, 64) = var_1_factors(trainingYields, [trainingPcaYields(:, 1) trainingSpcaMacro(:, 1)]);
    f(:, 65) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:2) trainingSpcaMacro(:, 1)]);
    f(:, 66) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:3) trainingSpcaMacro(:, 1)]);
    f(:, 67) = var_1_factors(trainingYields, [trainingPcaYields(:, 1) trainingSpcaMacro(:, 1:2)]);
    f(:, 68) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:2) trainingSpcaMacro(:, 1:2)]);
    f(:, 69) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:3) trainingSpcaMacro(:, 1:2)]);
    
    forecasts(i, :, :) = f;
end

pe = forecasts - y.Variables;

for j=1:size(dm, 1)
    for k=1:size(dm, 2)
        dm(j, k) = dmtest(pe(:, k, 1), pe(:, k, j + 1));
    end
end

spe = pe.^2;
mspe = squeeze(mean(spe, 1))';
rmspe = mspe ./ mspe(1, :);