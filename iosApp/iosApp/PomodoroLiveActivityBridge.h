#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface PomodoroLiveActivityBridge : NSObject

+ (instancetype)shared;

+ (BOOL)isSupported;

// endsAtEpochMs is 0 unless the Segment is running; nextSegmentType is set only at the Gate.
- (void)startLiveActivityWithSessionId:(NSString *)sessionId
                                 quote:(NSString *)quote
                           cycleNumber:(NSInteger)cycleNumber
                           totalCycles:(NSInteger)totalCycles
                           segmentType:(NSString *)segmentType
                      remainingSeconds:(NSInteger)remainingSeconds
                          totalSeconds:(NSInteger)totalSeconds
                         endsAtEpochMs:(int64_t)endsAtEpochMs
                              isPaused:(BOOL)isPaused
                       nextSegmentType:(NSString *_Nullable)nextSegmentType;

- (void)updateLiveActivityWithCycleNumber:(NSInteger)cycleNumber
                              totalCycles:(NSInteger)totalCycles
                              segmentType:(NSString *)segmentType
                         remainingSeconds:(NSInteger)remainingSeconds
                             totalSeconds:(NSInteger)totalSeconds
                            endsAtEpochMs:(int64_t)endsAtEpochMs
                                 isPaused:(BOOL)isPaused
                          nextSegmentType:(NSString *_Nullable)nextSegmentType;

- (void)endLiveActivity;

@end

NS_ASSUME_NONNULL_END
