//
//  SOSPicker.m
//  SyncOnSet
//
//  Created by Christopher Sullivan on 10/25/13.
//
//

#import "SOSPicker.h"

@implementation SOSPicker

@synthesize callbackId;

- (void) getPictures:(CDVInvokedUrlCommand *)command {
	NSDictionary *options = [command.arguments objectAtIndex: 0];

	NSInteger maximumImagesCount = [[options objectForKey:@"maximumImagesCount"] integerValue];
//	self.width = [[options objectForKey:@"width"] integerValue];
//	self.height = [[options objectForKey:@"height"] integerValue];
//	self.quality = [[options objectForKey:@"quality"] integerValue];
	
//	if (nil == result) {
//		result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:resultStrings];
//	}
//
//	[self.viewController dismissViewControllerAnimated:YES completion:nil];
//	[self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
    
    self.callbackId = command.callbackId;
    
    //[self.commandDelegate runInBackground:^{
    TZImagePickerController *imagePickerVc = [[TZImagePickerController alloc] initWithMaxImagesCount:maximumImagesCount delegate:self];
    
    UIColor *color = [UIColor colorWithRed:251 / 255.0 green:192 / 255.0 blue:45 / 255.0 alpha:1];
    
    imagePickerVc.modalPresentationStyle = UIModalPresentationFullScreen;
    imagePickerVc.allowTakePicture = NO;
    imagePickerVc.allowCameraLocation = NO;
    imagePickerVc.allowTakeVideo = NO;
    imagePickerVc.allowPickingVideo = NO;
    imagePickerVc.allowPickingOriginalPhoto = NO;
    imagePickerVc.oKButtonTitleColorNormal = color;
    imagePickerVc.iconThemeColor = color;
    //imagePickerVc.allowPickingGif = NO;
    imagePickerVc.naviBgColor = color;
    imagePickerVc.photoPreviewPageUIConfigBlock = ^(UICollectionView *collectionView, UIView *naviBar, UIButton *backButton, UIButton *selectButton, UILabel *indexLabel, UIView *toolBar, UIButton *originalPhotoButton, UILabel *originalPhotoLabel, UIButton *doneButton, UIImageView *numberImageView, UILabel *numberLabel) {
        naviBar.backgroundColor = color;
    };

    dispatch_async(dispatch_get_main_queue(), ^{
        [self.viewController presentViewController:imagePickerVc animated:YES completion:nil];
    });
    //}];
}

- (void) imagePickerController:(TZImagePickerController *)picker didFinishPickingPhotos:(NSArray<UIImage *> *)photos sourceAssets:(NSArray *)assets isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto infos:(NSArray<NSDictionary *> *)infos {
    CDVPluginResult *result = nil;
    
    NSMutableArray *resultPathArray = [[NSMutableArray alloc] init];
    NSString *tmpPath = [NSTemporaryDirectory() stringByStandardizingPath];
    
    NSData *data = nil;
    NSError *err = nil;
    
    NSString *filePath;
    NSString *fileName;
    
    for (UIImage *photo in photos) {
        fileName = [assets valueForKey:@"filename"];
        filePath = [NSString stringWithFormat:@"%@/%@%@", tmpPath, [[NSProcessInfo processInfo] globallyUniqueString], fileName];
        
        @autoreleasepool {
            data = UIImageJPEGRepresentation(photo, 1);
            if (![data writeToFile:filePath options:NSAtomicWrite error:&err]) {
                result = [CDVPluginResult resultWithStatus:CDVCommandStatus_IO_EXCEPTION messageAsString:[err localizedDescription]];
                break;
            } else {
                [resultPathArray addObject:[[NSURL fileURLWithPath:filePath] absoluteString]];
            }
        }
    }
    
    if (result == nil) {
        result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:resultPathArray];
    }

    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

- (void) tz_imagePickerControllerDidCancel:(TZImagePickerController *)picker {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsArray:[NSArray array]];
    [self.commandDelegate sendPluginResult:result callbackId:self.callbackId];
}

@end
