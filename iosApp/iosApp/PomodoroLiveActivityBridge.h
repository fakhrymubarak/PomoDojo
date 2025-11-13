#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PomodoroLiveActivityBridge : NSObject

+ (instancetype)shared;

+ (BOOL)isSupported;

- (void)startLiveActivityWithSessionId:(NSString *)sessionId
                                 quote:(NSString *)quote
                           cycleNumber:(NSInteger)cycleNumber
                           totalCycles:(NSInteger)totalCycles
                           segmentType:(NSString *)segmentType
                      remainingSeconds:(NSInteger)remainingSeconds
                          totalSeconds:(NSInteger)totalSeconds
                              isPaused:(BOOL)isPaused;

- (void)updateLiveActivityWithCycleNumber:(NSInteger)cycleNumber
                              totalCycles:(NSInteger)totalCycles
                              segmentType:(NSString *)segmentType
                         remainingSeconds:(NSInteger)remainingSeconds
                             totalSeconds:(NSInteger)totalSeconds
                                 isPaused:(BOOL)isPaused;

- (void)endLiveActivity;

- (void)endLiveActivityWithCompletionCycles:(NSInteger)completedCycles
                          totalFocusMinutes:(NSInteger)totalFocusMinutes
                          totalBreakMinutes:(NSInteger)totalBreakMinutes;

@end

NS_ASSUME_NONNULL_END
