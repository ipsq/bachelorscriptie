yields = import_yields();
macro = import_macro();

yieldsNorm = normalize(yields.Variables);
macroNorm = normalize(macro.Variables);
pcaYieldsCoeff = pca(yieldsNorm);
pcaMacroCoeff = pca(macroNorm);
spcaYieldsCoeff = spca(yieldsNorm, [], 3, inf, -[5, 3, 2]);
spcaMacroCoeff = spca(macroNorm, [], 3, inf, -[75, 50, 25]);

pcaYields = array2timetable(yieldsNorm*pcaYieldsCoeff, 'RowTimes', yields.date);
pcaMacro = array2timetable(macroNorm*pcaMacroCoeff, 'RowTimes', macro.date);
spcaYields = array2timetable(yieldsNorm*spcaYieldsCoeff, 'RowTimes', yields.date);
spcaMacro = array2timetable(macroNorm*spcaMacroCoeff, 'RowTimes', macro.date);

% sample = timerange('1992-01-01', '1999-12-01', 'months');
sample = timerange('2000-01-01', '2007-12-01', 'months');
% sample = timerange('2008-01-01', '2016-12-01', 'months');
% sample = timerange('1992-01-01', '1999-12-01', 'months');

cols = [1, 2, 3, 5, 10]; % tau = 1, 2, 3, 5, 10
y = yields(sample, cols);

nModels = 44;
forecasts = zeros(size(y, 1), size(y, 2), nModels);

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
    
    f = zeros(size(y, 2), nModels);
    f(:, 1) = ar_1(trainingYields);
    f(:, 2) = var_1(trainingYields);
    f(:, 3) = var_1_factors(trainingYields, trainingPcaMacro(:, 1));
    f(:, 4) = var_1_factors(trainingYields, trainingPcaMacro(:, 1:2));
    f(:, 5) = ar_sic(trainingYields);
    f(:, 6) = var_sic(trainingYields);
    f(:, 7) = var_sic_factors(trainingYields, trainingPcaMacro(:, 1));
    f(:, 8) = var_sic_factors(trainingYields, trainingPcaMacro(:, 1:2));
    f(:, 9) = dns_ar(trainingFullYields, [12 24 36 48 60 72 84 96 108 120]);
    f(:, 10) = dns_ar(trainingFullYields, [12 24 36 60 84 120]);
    f(:, 11) = dns_ar(trainingFullYields, [12 36 60 120]);
    f(:, 12) = dns_var(trainingFullYields, [12 24 36 48 60 72 84 96 108 120]);
    f(:, 13) = dns_var(trainingFullYields, [12 24 36 60 84 120]);
    f(:, 14) = dns_var(trainingFullYields, [12 36 60 120]);
    
    f(:, 15) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1));
    f(:, 16) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1));
    f(:, 17) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1));
    f(:, 18) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1));
    f(:, 19) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1));
    f(:, 20) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1));
    
    f(:, 21) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1:2));
    f(:, 22) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1:2));
    f(:, 23) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1:2));
    f(:, 24) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingPcaMacro(:, 1:2));
    f(:, 25) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingPcaMacro(:, 1:2));
    f(:, 26) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingPcaMacro(:, 1:2));
    
    f(:, 27) = dns_ar_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingMacro);
    f(:, 28) = dns_ar_factors(trainingFullYields, [12 24 36 60 84 120], trainingMacro);
    f(:, 29) = dns_ar_factors(trainingFullYields, [12 36 60 120], trainingMacro);
    f(:, 30) = dns_var_factors(trainingFullYields, [12 24 36 48 60 72 84 96 108 120], trainingMacro);
    f(:, 31) = dns_var_factors(trainingFullYields, [12 24 36 60 84 120], trainingMacro);
    f(:, 32) = dns_var_factors(trainingFullYields, [12 36 60 120], trainingMacro);
    
    f(:, 33) = var_1_factors(trainingYields, trainingPcaYields(:, 1));
    f(:, 34) = var_1_factors(trainingYields, trainingPcaYields(:, 1:2));
    f(:, 35) = var_1_factors(trainingYields, trainingPcaYields(:, 1:3));
    f(:, 36) = var_1_factors(trainingYields, trainingPcaMacro(:, 1));
    f(:, 37) = var_1_factors(trainingYields, trainingPcaMacro(:, 1:2));
    f(:, 38) = var_1_factors(trainingYields, trainingPcaMacro(:, 1:3));
    
    f(:, 39) = var_1_factors(trainingYields, [trainingPcaYields(:, 1) trainingPcaMacro(:, 1)]);
    f(:, 40) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:2) trainingPcaMacro(:, 1)]);
    f(:, 41) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:3) trainingPcaMacro(:, 1)]);
    f(:, 42) = var_1_factors(trainingYields, [trainingPcaYields(:, 1) trainingPcaMacro(:, 1:2)]);
    f(:, 43) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:2) trainingPcaMacro(:, 1:2)]);
    f(:, 44) = var_1_factors(trainingYields, [trainingPcaYields(:, 1:3) trainingPcaMacro(:, 1:2)]);
    
    forecasts(i, :, :) = f;
end

spe = (forecasts - y.Variables).^2;
mspe = squeeze(mean(spe, 1))';
rmspe = mspe ./ mspe(1, :);