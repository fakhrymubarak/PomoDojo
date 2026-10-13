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
                         endsAtEpochMs:(int64_t)endsAtEpochMs
                              isPaused:(BOOL)isPaused
                       nextSegmentType:(NSString *_Nullable)nextSegmentType {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] startLiveActivityWithSessionId:sessionId
                                                                       quote:quote
                                                                 cycleNumber:cycleNumber
                                                                 totalCycles:totalCycles
                                                                 segmentType:segmentType
                                                            remainingSeconds:remainingSeconds
                                                                totalSeconds:totalSeconds
                                                               endsAtEpochMs:endsAtEpochMs
                                                                    isPaused:isPaused
                                                             nextSegmentType:nextSegmentType];
    } else {
        NSLog(@"PomodoroLiveActivityBridge: Live Activities not supported on this iOS version");
    }
}

- (void)updateLiveActivityWithCycleNumber:(NSInteger)cycleNumber
                              totalCycles:(NSInteger)totalCycles
                              segmentType:(NSString *)segmentType
                         remainingSeconds:(NSInteger)remainingSeconds
                             totalSeconds:(NSInteger)totalSeconds
                            endsAtEpochMs:(int64_t)endsAtEpochMs
                                 isPaused:(BOOL)isPaused
                          nextSegmentType:(NSString *_Nullable)nextSegmentType {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] updateLiveActivityWithCycleNumber:cycleNumber
                                                                    totalCycles:totalCycles
                                                                    segmentType:segmentType
                                                               remainingSeconds:remainingSeconds
                                                                   totalSeconds:totalSeconds
                                                                  endsAtEpochMs:endsAtEpochMs
                                                                       isPaused:isPaused
                                                                nextSegmentType:nextSegmentType];
    }
}

- (void)endLiveActivity {
    if (@available(iOS 16.2, *)) {
        [[PomodoroLiveActivityManager shared] endLiveActivity];
    }
}

@end
