#import "PomodoroLiveActivityBridge.h"
#import "PomoDojo-Swift.h"

@implementation PomodoroLiveActivityBridge

+ (instancetype)shared {
    static PomodoroLiveActivityBridge *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

+ (BOOL)isSupported {
    if (@available(iOS 16.2, *)) {
        return [PomodoroLiveActivityManager isSupported];
    }
    return NO;
}

- (void)startLiveActivityWithSessionId:(NSString *)sessionId
                                 quote:(NSString *)quote
                           cycleNumber:(NSInteger)cycleNumber
                           totalCycles:(NSInteger)totalCycles
                           segmentType:(NSString *)segmentType
                      remainingSeconds:(NSInteger)remainingSeconds
                          totalSeconds:(NSInteger)totalSeconds
        isPaused:(BOOL)isPaused
    scheduleJSON:(NSString *_Nullable)scheduleJSON {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] startLiveActivityWithSessionId:sessionId
                                                                       quote:quote
                                                                 cycleNumber:cycleNumber
                                                                 totalCycles:totalCycles
                                                                 segmentType:segmentType
                                                            remainingSeconds:remainingSeconds
                                                                totalSeconds:totalSeconds
                isPaused:isPaused
            scheduleJSON:scheduleJSON];
    } else {
        NSLog(@"PomodoroLiveActivityBridge: Live Activities not supported on this iOS version");
    }
}

- (void)updateLiveActivityWithCycleNumber:(NSInteger)cycleNumber
                              totalCycles:(NSInteger)totalCycles
                              segmentType:(NSString *)segmentType
                         remainingSeconds:(NSInteger)remainingSeconds
                             totalSeconds:(NSInteger)totalSeconds
                                 isPaused:(BOOL)isPaused
                             scheduleJSON:(NSString *_Nullable)scheduleJSON {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] updateLiveActivityWithCycleNumber:cycleNumber
                                                                    totalCycles:totalCycles
                                                                    segmentType:segmentType
                                                               remainingSeconds:remainingSeconds
                                                                   totalSeconds:totalSeconds
                isPaused:isPaused
            scheduleJSON:scheduleJSON];
    }
}

- (void)endLiveActivity {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] endLiveActivity];
    }
}

- (void)endLiveActivityWithCompletionCycles:(NSInteger)completedCycles
                          totalFocusMinutes:(NSInteger)totalFocusMinutes
                          totalBreakMinutes:(NSInteger)totalBreakMinutes {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] endLiveActivityWithCompletionWithCompletedCycles:completedCycles
                                                                             totalFocusMinutes:totalFocusMinutes
                                                                             totalBreakMinutes:totalBreakMinutes];
    }
}

@end
