yields = import_yields();
macro = import_macro();

[pcaYieldsCoeff,pcaYieldsScores,~] = pca(yields.Variables);
[pcaMacroCoeff,pcaMacroScores,~] = pca(macro.Variables);

pcaYields = array2timetable(pcaYieldsScores, 'RowTimes', yields.date);
pcaMacro = array2timetable(pcaMacroScores, 'RowTimes', macro.date);

sample = timerange('1992-03-01', '1999-12-01', 'months');
% sample = timerange('1992-03-01', '1999-12-01', 'months');
% sample = timerange('1992-03-01', '1999-12-01', 'months');
% sample = timerange('1992-03-01', '1999-12-01', 'months');

cols = [1, 2, 3, 5, 10]; % tau = 1, 2, 3, 5, 10
y = yields(sample, cols);

nModels = 2;
forecast = zeros(size(y, 1), size(y, 2), nModels);

for i=1:size(y,1)
    trainingSample = timerange(datestr(addtodate(datenum(y.date(i)), -120, 'month')), ...
        datestr(addtodate(datenum(y.date(i)), -1, 'month')), 'months'); % rolling window of 120 months
    trainingYields = yields(trainingSample, cols).Variables;
    trainingFullYields = yields(trainingSample, :).Variables; % required by some models, like dns
    trainingMacro = macro(trainingSample, :).Variables; % macroeconomic variables
    
    % fit each model, forecast 1-step-ahead and compute mspe
    forecast(i, :, 1) = ar_1(trainingYields);
    forecast(i, :, 2) = var_1(trainingYields);
end

spe = (forecast - y.Variables).^2;
mspe = squeeze(mean(spe, 1))';