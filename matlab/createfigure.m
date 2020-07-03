function createfigure(zdata1)
    % Create figure
    figure1 = figure;

    % Create axes
    axes1 = axes('Parent',figure1);
    hold(axes1,'on');

    % Create surf
    surf(zdata1,'Parent',axes1);

    % Create zlabel
    zlabel({'Yield'});

    % Create xlabel
    xlabel({'Maturity (in years)'});

    xlim(axes1,[1 10]);
    view(axes1,[120 25]);
    grid(axes1,'on');
    hold(axes1,'off');
end